package net.legacylauncher.ui.account;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.user.MinecraftUser;
import net.legacylauncher.user.MojangUser;
import net.legacylauncher.user.UserSet;
import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.oareq.CodeRequestCancelledException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestStrategy;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.*;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd.NanoHttpdLocalServer;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchanger;
import net.legacylauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import net.legacylauncher.user.minecraft.strategy.preq.ProfileNotCreatedException;
import net.legacylauncher.user.minecraft.strategy.preq.create.MinecraftProfileCreator;
import net.legacylauncher.user.minecraft.strategy.preq.create.ProfileCreationAbortedException;
import net.legacylauncher.user.minecraft.strategy.preq.create.ProfileCreatorUserInterface;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import net.legacylauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import net.legacylauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

@Slf4j
class AccountMinecraftProcessWorker {
    private final AccountMinecraftProcess parent;
    private final String locPrefix;

    private Future<?> currentProcess;

    AccountMinecraftProcessWorker(AccountMinecraftProcess parent) {
        this.parent = parent;
        this.locPrefix = parent.LOC_PREFIX;
    }

    private void run() {
        try {
            doRun();
        } catch (InterruptedException | TimeoutException e) {
            setState("cancelled");
            stopProgress();
        } catch (MinecraftAuthenticationException e) {
            stopProgress();
            if (e instanceof GameOwnershipValidationException && ((GameOwnershipValidationException) e).isKnownNotToOwn()) {
                log.info("User does not own Minecraft or not yet migrated their Mojang account");
                setState("dont-own");
                Alert.showLocError(
                        locPrefix + "dont-own.alert.title",
                        locPrefix + "dont-own.alert.message",
                        null
                );
            } else if (e instanceof CodeRequestCancelledException) {
                setState("cancelled");
                log.info("User cancelled OAuth code request: {}", e.toString());
            } else {
                log.warn("Authentication failed", e);
                showError(e);
            }
        } catch (Exception e) {
            log.error("Something went wrong while authenticating", e);
            showError(e);
        }
    }

    private void showError(Throwable e) {
        setState("error");
        stopProgress();
        if (e instanceof MinecraftAuthenticationException) {
            String key = ((MinecraftAuthenticationException) e).getShortKey();
            Alert.showLocError(
                    "account.manager.error.title",
                    "account.manager.error.minecraft." + key,
                    e.toString()
            );
        } else {
            Alert.showLocError(locPrefix + "error.title", locPrefix + "error.message", e.toString());
        }
    }

    private void stopProgress() {
        SwingUtil.later(parent::stopProgress);
    }

    private void doRun() throws Exception {
        OAuthApplication application = OAuthApplication.TL;
        setState("browser-open");
        MicrosoftOAuthCodeRequestStrategy requestStrategy = initBrowser();
        if (requestStrategy instanceof LocalServerStrategy) {
            LocalServerSelectedConfiguration selectedConfig = ((LocalServerStrategy) requestStrategy).startServer();
            String loginUrl = urlProducer.buildLoginUrl(selectedConfig);
            SwingUtil.later(() -> parent.setButtonLink(loginUrl));
        }
        MicrosoftOAuthExchangeCode msftExhangeCode = requestStrategy.requestMicrosoftOAuthCode();
        SwingUtil.later(() -> parent.setButtonLink(null));
        setState("exchanging-code");
        MicrosoftOAuthToken msftToken = new MicrosoftOAuthCodeExchanger(application).exchangeMicrosoftOAuthCode(msftExhangeCode);
        setState("xbox-live-auth");
        XboxServiceAuthenticationResponse xboxLiveToken = new XboxLiveAuthenticator(application).xboxLiveAuthenticate(msftToken.getAccessToken());
        XboxServiceAuthenticationResponse xstsToken = new XSTSAuthenticator().xstsAuthenticate(xboxLiveToken.getToken());
        setState("mcs-auth");
        MinecraftServicesToken minecraftToken = new MinecraftServicesAuthenticator().minecraftServicesAuthenticate(xstsToken);
        setState("game-ownership");
        new GameOwnershipValidator().checkGameOwnership(minecraftToken);
        setState("getting-profile");
        MinecraftOAuthProfile minecraftProfile;
        try {
            minecraftProfile = new MinecraftProfileRequester().requestProfile(minecraftToken);
        } catch (ProfileNotCreatedException e) {
            log.info("User starts Minecraft for the first time");
            minecraftProfile = handleProfileCreation(minecraftToken);
        }
        MinecraftUser minecraftUser = new MinecraftProfileConverter().convertToMinecraftUser(msftToken, minecraftToken, minecraftProfile);
        SwingUtil.wait(() -> {
            StandardAccountPane.removeAccountIfFound(minecraftUser.getUsername(), Account.AccountType.MINECRAFT);
            UserSet userSet = LegacyLauncher.getInstance().getProfileManager().getAccountManager().getUserSet();
            userSet.add(minecraftUser);
            userSet.getSet().stream().filter(u ->
                    u.getType().equals(MojangUser.TYPE) && u.getUUID().equals(minecraftUser.getUUID())
            ).findAny().ifPresent(userSet::remove);
            parent.scene.multipane.showTip("success-add");
            parent.scene.list.select(new Account<>(minecraftUser));
        });
    }

    private final LocalServerUrlProducer urlProducer = new LocalServerUrlProducer();

    private MicrosoftOAuthCodeRequestStrategy initBrowser() {
        return new LocalServerStrategy(
                new DefaultExternalBrowser(),
                urlProducer,
                new NanoHttpdLocalServer(
                        new OAuthUrlParser(),
                        urlProducer
                ),
                new LocalServerConfiguration(
                        "localhost",
                        Arrays.asList(46521, 47522, 48523, 49524),
                        "",
                        "https://llaun.ch/msft-auth-success"
                )
        );
    }

    private MinecraftOAuthProfile handleProfileCreation(MinecraftServicesToken token) throws IOException, ProfileCreationAbortedException {
        return new MinecraftProfileCreator(
                new ProfileCreatorUserInterface() {
                    final String prefix = locPrefix + "profile-create.";
                    boolean showHead = true;

                    @Override
                    public String requestProfileName() {
                        boolean showHead = this.showHead;
                        this.showHead = false;
                        return Alert.showInputQuestion(
                                "",
                                (showHead ? Localizable.get(prefix + "question.head") + "\n\n" : "") +
                                        Localizable.get(prefix + "question.body")
                        );
                    }

                    @Override
                    public void showProfileUnavailableMessage(String profileName) {
                        log.info("Profile is not available: {}", profileName);
                        Alert.showError("", Localizable.get(prefix + "unavailable"));
                    }
                }
        ).createProfile(token);
    }

    private void setState(String label) {
        SwingUtil.later(() -> parent.setProcessLabel(label));
    }

    void start() {
        cancel();
        currentProcess = AsyncThread.future(this::run);
    }

    void cancel() {
        if (currentProcess != null) {
            currentProcess.cancel(true);
            currentProcess = null;
        }
    }
}
