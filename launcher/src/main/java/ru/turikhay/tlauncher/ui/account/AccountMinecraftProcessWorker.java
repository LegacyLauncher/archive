package ru.turikhay.tlauncher.ui.account;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.user.MinecraftUser;
import ru.turikhay.tlauncher.user.MojangUser;
import ru.turikhay.tlauncher.user.UserSet;
import ru.turikhay.tlauncher.user.minecraft.oauth.OAuthApplication;
import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import ru.turikhay.tlauncher.user.minecraft.strategy.gos.GameOwnershipValidationException;
import ru.turikhay.tlauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.CodeRequestCancelledException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestStrategy;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv.*;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd.NanoHttpdLocalServer;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchanger;
import ru.turikhay.tlauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.ProfileNotCreatedException;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.create.MinecraftProfileCreator;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.create.ProfileCreationAbortedException;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.create.ProfileCreatorUserInterface;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

class AccountMinecraftProcessWorker {
    private static final Logger LOGGER = LogManager.getLogger(AccountMinecraftProcessWorker.class);

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
                LOGGER.info("User does not own Minecraft or not yet migrated their Mojang account");
                setState("dont-own");
                Alert.showLocError(
                        locPrefix + "dont-own.alert.title",
                        locPrefix + "dont-own.alert.message",
                        null
                );
            } else if (e instanceof CodeRequestCancelledException) {
                setState("cancelled");
                LOGGER.info("User cancelled OAuth code request: {}", e.toString());
            } else {
                LOGGER.warn("Authentication failed", e);
                showError(e);
            }
        } catch (Exception e) {
            LOGGER.error("Something went wrong while authenticating", e);
            showError(e);
        }
    }

    private void showError(Exception e) {
        setState("error");
        stopProgress();
        Sentry.capture(new EventBuilder()
                .withLevel(Event.Level.ERROR)
                .withMessage("microsoft auth failed")
                .withSentryInterface(new ExceptionInterface(e))
        );
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
            LOGGER.info("User starts Minecraft for the first time");
            minecraftProfile = handleProfileCreation(minecraftToken);
        }
        MinecraftUser minecraftUser = new MinecraftProfileConverter().convertToMinecraftUser(msftToken, minecraftToken, minecraftProfile);
        SwingUtil.wait(() -> {
            StandardAccountPane.removeAccountIfFound(minecraftUser.getUsername(), Account.AccountType.MINECRAFT);
            UserSet userSet = TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet();
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
                        LOGGER.info("Profile is not available: {}", profileName);
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
