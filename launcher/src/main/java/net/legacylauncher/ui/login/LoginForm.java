package net.legacylauncher.ui.login;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.downloader.Downloadable;
import net.legacylauncher.downloader.Downloader;
import net.legacylauncher.downloader.DownloaderListener;
import net.legacylauncher.managers.SwingVersionManagerListener;
import net.legacylauncher.managers.VersionManager;
import net.legacylauncher.managers.VersionManagerListener;
import net.legacylauncher.minecraft.Server;
import net.legacylauncher.minecraft.auth.Authenticator;
import net.legacylauncher.minecraft.auth.AuthenticatorListener;
import net.legacylauncher.minecraft.crash.Crash;
import net.legacylauncher.minecraft.crash.CrashManager;
import net.legacylauncher.minecraft.crash.CrashManagerListener;
import net.legacylauncher.minecraft.crash.SwingCrashManagerListener;
import net.legacylauncher.minecraft.launcher.MinecraftException;
import net.legacylauncher.minecraft.launcher.MinecraftListener;
import net.legacylauncher.ui.MainPane;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.center.CenterPanel;
import net.legacylauncher.ui.login.buttons.ButtonPanel;
import net.legacylauncher.ui.scenes.DefaultScene;
import net.legacylauncher.ui.settings.SettingsPanel;
import net.legacylauncher.ui.swing.DelayedComponent;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.user.AuthException;
import net.legacylauncher.user.User;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncThread;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class LoginForm extends CenterPanel implements MinecraftListener, AuthenticatorListener<User>, VersionManagerListener, DownloaderListener, CrashManagerListener {
    private final List<LoginForm.LoginStateListener> stateListeners = Collections.synchronizedList(new ArrayList<>());
    private final List<LoginForm.LoginProcessListener> processListeners = Collections.synchronizedList(new ArrayList<>());
    public final DefaultScene scene;
    public final MainPane pane;
    private final DelayedComponent<SettingsPanel> settings;
    public final AccountComboBox accounts;
    public final VersionComboBox versions;
    public final CheckBoxPanel checkbox;
    public final ButtonPanel buttons;
    public final AutoLogin autologin;
    private LoginForm.LoginState state;
    private Server server;
    private int serverId;
    public static final String LOGIN_BLOCK = "login";
    public static final String REFRESH_BLOCK = "refresh";
    public static final String LAUNCH_BLOCK = "launch";
    public static final String AUTH_BLOCK = "auth";
    public static final String UPDATER_BLOCK = "update";
    public static final String DOWNLOADER_BLOCK = "download";

    public LoginForm(DefaultScene scene) {
        state = LoginForm.LoginState.STOPPED;
        this.scene = scene;
        pane = scene.getMainPane();
        settings = scene.settingsForm;
        autologin = new AutoLogin(this);
        accounts = new AccountComboBox(this);
        buttons = new ButtonPanel(this);
        versions = new VersionComboBox(this);
        checkbox = new CheckBoxPanel(this);
        processListeners.add(new SwingLoginProcessListener(new LoginProcessListener() {
            @Override
            public void loggingIn() throws LoginException {
                pane.progress.get().loggingIn();
            }

            @Override
            public void loginFailed() {
                pane.progress.get().loginFailed();
            }

            @Override
            public void loginSucceed() {
                pane.progress.get().loginSucceed();
            }
        }));
        processListeners.add(autologin);
        processListeners.add(new SwingLoginProcessListener(new LoginProcessListener() {
            @Override
            public void loggingIn() throws LoginException {
                settings.get().loggingIn();
            }

            @Override
            public void loginFailed() {
                settings.get().loginFailed();
            }

            @Override
            public void loginSucceed() {
                settings.get().loginSucceed();
            }
        }));
        processListeners.add(checkbox);
        processListeners.add(versions);
        processListeners.add(accounts);
        processListeners.add(new LoginListener() {
            @Override
            public void loggingIn() throws LoginException {
                VersionSyncInfo sync = versions.getVersion();
                if (!sync.isInstalled() || !sync.hasRemote()) {
                    return;
                }
                if (ReleaseType.SubType.get(sync.getLocal()).contains(ReleaseType.SubType.OLD_RELEASE)) {
                    return;
                }

                CompleteVersion local;
                try {
                    local = sync.resolveCompleteVersion(tlauncher.getVersionManager(), false);
                } catch (Exception e) {
                    throw new RuntimeException("could not resolve local version", e);
                }

                if (local.getReleaseType() != ReleaseType.OLD_ALPHA &&
                        local.getReleaseType() != ReleaseType.OLD_BETA &&
                        !ReleaseType.SubType.OLD_RELEASE.isSubType(local) &&
                        local.getAssetIndex() != null &&
                        "legacy".equals(local.getAssetIndex().getId())) {
                    if (Alert.showLocQuestion("versions.damaged-json")) {
                        SwingUtil.wait(() -> checkbox.forceupdate.setSelected(true));
                    }
                }
            }
        });
        stateListeners.add(buttons.play);
        add(messagePanel);
        add(del(0));
        ExtendedPanel p = new ExtendedPanel();
        p.setLayout(new GridLayout(2, 1, 0, 2));
        p.add(accounts);
        p.add(versions);
        add(p);
        add(del(0));
        add(checkbox);
        add(del(0));
        add(buttons);
        tlauncher.getVersionManager().addListener(new SwingVersionManagerListener(this));
        tlauncher.getDownloader().addListener(this);
        tlauncher.getUiListeners().registerMinecraftLauncherListener(this);
    }

    private void runProcess() {
        Exception error = null;
        boolean success = true;
        synchronized (processListeners) {
            for (LoginProcessListener listener : processListeners) {
                try {
                    //log("Listener:", listener);
                    listener.loggingIn();
                } catch (LoginWaitException loginError) {
                    log.debug("Caught a wait task");

                    block("wait-task");
                    try {
                        loginError.getWaitTask().runTask();
                    } catch (Exception e) {
                        log.error("Caught an error on a wait task of {}", listener, e);
                        error = e;
                    } finally {
                        unblock("wait-task");
                    }
                } catch (LoginException e) {
                    log.error("Caught an error on listener {}", listener, e);
                    error = e;
                }

                if (error != null) {
                    success = false;
                    break;
                }
            }

            if (success) {
                for (LoginProcessListener processListener : processListeners) {
                    processListener.loginSucceed();
                }
            } else {
                for (LoginProcessListener processListener : processListeners) {
                    processListener.loginFailed();
                }
            }
        }

        if (error != null) {
            log.warn("Login process has ended with an error.");
        } else {
            if (accounts.getAccount() != null) {
                global.setForcefully("login.account", accounts.getAccount().getUsername(), false);
                global.setForcefully("login.account.type", accounts.getAccount().getType(), false);
            }
            global.setForcefully("login.version", versions.getVersion().getID(), false);
            global.store();

            changeState(LoginForm.LoginState.LAUNCHING);

            log.debug("Calling Minecraft Launcher...");
            String versionName = requestedVersion == null ? versions.getVersion().getID() : requestedVersion.getID();
            boolean forceUpdate = checkbox.forceupdate.isSelected();
            AsyncThread.execute(() -> tlauncher.newMinecraftLauncher(versionName, server, serverId, forceUpdate).start());
            checkbox.forceupdate.setSelected(false);
        }
        requestedVersion = null;
    }

    private void stopProcess() {
        while (!tlauncher.isMinecraftLauncherWorking()) {
            U.sleepFor(500L);
        }

        changeState(LoginForm.LoginState.STOPPING);
        tlauncher.getMinecraftLauncher().stop();
    }

    VersionSyncInfo requestedVersion;

    public void startLauncher() {
        startLauncher(null, 0);
    }

    public void startLauncher(Server server, int serverId) {
        startLauncher(null, server, serverId);
    }

    public void startLauncher(VersionSyncInfo requestedVersion, Server server, int serverId) {
        if (!Blocker.isBlocked(this)) {
            log.debug("Starting launcher: {}", requestedVersion);
            Blocker.block(this, "starting");
            AsyncThread.execute(() -> {
                while (tlauncher.getLibraryManager().isRefreshing()) {
                    log.debug("Waiting for library manager...");
                    U.sleepFor(500L);
                }
                SwingUtil.later(() -> {
                    try {
                        this.requestedVersion = requestedVersion;
                        if (requestedVersion != null) {
                            versions.setSelectedValue(requestedVersion);
                        }
                        this.server = server;
                        this.serverId = serverId;
                        autologin.setActive(false);
                        AsyncThread.execute(this::runProcess);
                    } finally {
                        Blocker.unblock(this, "starting");
                    }
                });
            });
        }
    }

    public void stopLauncher() {
        AsyncThread.execute(this::stopProcess);
    }

    private void changeState(LoginForm.LoginState state) {
        if (state == null) {
            throw new NullPointerException();
        } else if (this.state != state) {
            this.state = state;

            for (LoginStateListener listener : stateListeners) {
                listener.loginStateChanged(state);
            }

        }
    }

    public void block(Object reason) {
        if (!Blocker.getBlockList(this).contains("refresh")) {
            Blocker.block(accounts, reason);
        }

        Blocker.block(reason, settings, versions, checkbox, buttons, scene.noticePanel);
    }

    public synchronized void unblock(Object reason) {
        Blocker.unblock(reason, settings, accounts, versions, checkbox, buttons, scene.noticePanel);
    }

    public void onDownloaderStart(Downloader d, int files) {
        Blocker.block(this, "download");
    }

    public void onDownloaderAbort(Downloader d) {
        Blocker.unblock(this, "download");
    }

    public void onDownloaderProgress(Downloader d, double progress, double speed) {
    }

    public void onDownloaderFileComplete(Downloader d, Downloadable file) {
    }

    public void onDownloaderComplete(Downloader d) {
        Blocker.unblock(this, "download");
    }

    public void onVersionsRefreshing(VersionManager manager) {
        Blocker.block(this, "refresh");
    }

    public void onVersionsRefreshingFailed(VersionManager manager) {
        Blocker.unblock(this, "refresh");
    }

    public void onVersionsRefreshed(VersionManager manager) {
        Blocker.unblock(this, "refresh");
    }

    public void onAuthPassing(Authenticator<? extends User> auth) {
        Blocker.block(this, "auth");
    }

    public void onAuthPassingError(Authenticator<? extends User> auth, Throwable e) {
        if (e instanceof AuthException && ((AuthException) e).isSoft() && Alert.showLocQuestion("account.exception.soft")) {
            return;
        }
        Blocker.unblock(this, "auth");
        throw new LoginException("Cannot auth!");
    }

    public void onAuthPassed(Authenticator<? extends User> auth) {
        Blocker.unblock(this, "auth");
    }

    public void onMinecraftPrepare() {
        Blocker.block(this, "launch");
    }

    public void onMinecraftAbort() {
        Blocker.unblock(this, "launch");
        buttons.play.updateState();
    }

    public void onMinecraftLaunch() {
        changeState(LoginForm.LoginState.LAUNCHED);
    }

    public void onMinecraftClose() {
        Blocker.unblock(this, "launch");
        changeState(LoginForm.LoginState.STOPPED);
        if (autologin.isEnabled()) {
            tlauncher.getVersionManager().asyncRefresh();
        } else {
            tlauncher.getVersionManager().asyncRefresh(true);
        }

    }

    public void onMinecraftError(Throwable throwable) {
        Blocker.unblock(this, "launch");
        changeState(LoginForm.LoginState.STOPPED);
    }

    public void onMinecraftKnownError(MinecraftException exception) {
        Blocker.unblock(this, "launch");
        changeState(LoginForm.LoginState.STOPPED);
    }

    @Override
    public void onCrashManagerInit(CrashManager manager) {
        Blocker.unblock(this, "launch");
        changeState(LoginForm.LoginState.STOPPED);
        manager.addListener(new SwingCrashManagerListener(this));
    }

    @Override
    public void onCrashManagerProcessing(CrashManager manager) {
        Blocker.block(this, "crash");
    }

    @Override
    public void onCrashManagerComplete(CrashManager manager, Crash crash) {
        Blocker.unblock(this, "crash");
    }

    @Override
    public void onCrashManagerCancelled(CrashManager manager) {
        Blocker.unblock(this, "crash");
    }

    @Override
    public void onCrashManagerFailed(CrashManager manager, Exception e) {
        Blocker.unblock(this, "crash");
    }

    public abstract static class LoginListener implements LoginForm.LoginProcessListener {
        public abstract void loggingIn() throws LoginException;

        public void loginFailed() {
        }

        public void loginSucceed() {
        }
    }

    public interface LoginProcessListener {
        void loggingIn() throws LoginException;

        void loginFailed();

        void loginSucceed();
    }

    public static class SwingLoginProcessListener implements LoginProcessListener {
        private final LoginProcessListener delegate;

        public SwingLoginProcessListener(LoginProcessListener delegate) {
            this.delegate = delegate;
        }

        public void loggingIn() throws LoginException {
            SwingUtil.wait(delegate::loggingIn);
        }

        public void loginFailed() {
            SwingUtil.wait(delegate::loginFailed);
        }

        public void loginSucceed() {
            SwingUtil.wait(delegate::loginSucceed);
        }
    }

    public enum LoginState {
        LAUNCHING,
        STOPPING,
        STOPPED,
        LAUNCHED
    }

    public interface LoginStateListener {
        void loginStateChanged(LoginForm.LoginState state);
    }
}
