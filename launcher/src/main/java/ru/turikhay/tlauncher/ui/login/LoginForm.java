package ru.turikhay.tlauncher.ui.login;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.DownloaderListener;
import ru.turikhay.tlauncher.managers.SwingVersionManagerListener;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.minecraft.Server;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashManager;
import ru.turikhay.tlauncher.minecraft.crash.CrashManagerListener;
import ru.turikhay.tlauncher.minecraft.crash.SwingCrashManagerListener;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.tlauncher.ui.swing.DelayedComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.tlauncher.user.User;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.async.LoopedThread;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoginForm extends CenterPanel implements MinecraftListener, AuthenticatorListener<User>, VersionManagerListener, DownloaderListener, CrashManagerListener {
    private static final Logger LOGGER = LogManager.getLogger(LoginForm.class);
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
    private final LoginForm.StartThread startThread;
    private final LoginForm.StopThread stopThread;
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
        startThread = new LoginForm.StartThread();
        stopThread = new LoginForm.StopThread();
        autologin = new AutoLogin(this);
        accounts = new AccountComboBox(this);
        buttons = new ButtonPanel(this);
        versions = new VersionComboBox(this);
        checkbox = new CheckBoxPanel(this);
        processListeners.add(autologin);
        processListeners.add(new LoginProcessListener() {
            @Override
            public void logginingIn() throws LoginException {
                settings.get().logginingIn();
            }

            @Override
            public void loginFailed() {
                settings.get().loginFailed();
            }

            @Override
            public void loginSucceed() {
                settings.get().loginSucceed();
            }
        });
        processListeners.add(checkbox);
        processListeners.add(versions);
        processListeners.add(accounts);
        processListeners.add(new LoginListener() {
            @Override
            public void logginingIn() throws LoginException {
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
                        checkbox.forceupdate.setSelected(true);
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
        tlauncher.getUIListeners().registerMinecraftLauncherListener(this);
    }

    private void runProcess() {
        LoginException error = null;
        boolean success = true;
        synchronized (processListeners) {
            for (LoginProcessListener listener : processListeners) {
                try {
                    //log("Listener:", listener);
                    listener.logginingIn();
                } catch (LoginWaitException loginError) {
                    LOGGER.debug("Caught a wait task");

                    block("wait-task");
                    try {
                        loginError.getWaitTask().runTask();
                    } catch (LoginException e) {
                        LOGGER.error("Caught an error on a wait task of {}", listener, e);
                        error = e;
                    } finally {
                        unblock("wait-task");
                    }
                } catch (LoginException e) {
                    LOGGER.error("Caught an error on listener {}", listener, e);
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
            LOGGER.warn("Login process has ended with an error.");
        } else {
            if (accounts.getAccount() != null) {
                global.setForcefully("login.account", accounts.getAccount().getUsername(), false);
                global.setForcefully("login.account.type", accounts.getAccount().getType(), false);
            }
            global.setForcefully("login.version", versions.getVersion().getID(), false);
            global.store();

            changeState(LoginForm.LoginState.LAUNCHING);

            LOGGER.debug("Calling Minecraft Launcher...");
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
            LOGGER.debug("Starting launcher: {}", requestedVersion);
            Blocker.block(this, "starting");
            AsyncThread.execute(() -> {
                while (tlauncher.getLibraryManager().isRefreshing()) {
                    LOGGER.debug("Waiting for library manager...");
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
                        startThread.iterate();
                    } finally {
                        Blocker.unblock(this, "starting");
                    }
                });
            });
        }
    }

    public void stopLauncher() {
        stopThread.iterate();
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

    public void onMinecraftError(Throwable e) {
        Blocker.unblock(this, "launch");
        changeState(LoginForm.LoginState.STOPPED);
    }

    public void onMinecraftKnownError(MinecraftException e) {
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
        public abstract void logginingIn() throws LoginException;

        public void loginFailed() {
        }

        public void loginSucceed() {
        }
    }

    public interface LoginProcessListener {
        void logginingIn() throws LoginException;

        void loginFailed();

        void loginSucceed();
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

    class StartThread extends LoopedThread {
        StartThread() {
            startAndWait();
        }

        protected void iterateOnce() {
            try {
                runProcess();
            } catch (Throwable e) {
                Alert.showError(e);
            }

        }
    }

    class StopThread extends LoopedThread {
        StopThread() {
            startAndWait();
        }

        protected void iterateOnce() {
            try {
                stopProcess();
            } catch (Throwable e) {
                Alert.showError(e);
            }

        }
    }
}
