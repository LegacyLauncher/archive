package ru.turikhay.tlauncher.ui.login;

import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.DownloaderListener;
import ru.turikhay.tlauncher.managers.*;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashManager;
import ru.turikhay.tlauncher.minecraft.crash.CrashManagerListener;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LoginForm extends CenterPanel implements MinecraftListener, AuthenticatorListener, VersionManagerListener, DownloaderListener, ElyManagerListener, CrashManagerListener {
    private final List<LoginForm.LoginStateListener> stateListeners = Collections.synchronizedList(new ArrayList());
    private final List<LoginForm.LoginProcessListener> processListeners = Collections.synchronizedList(new ArrayList());
    public final DefaultScene scene;
    public final MainPane pane;
    private final SettingsPanel settings;
    public final AccountComboBox accounts;
    public final VersionComboBox versions;
    public final CheckBoxPanel checkbox;
    public final ButtonPanel buttons;
    public final AutoLogin autologin;
    private final LoginForm.StartThread startThread;
    private final LoginForm.StopThread stopThread;
    private LoginForm.LoginState state;
    private ServerList.Server server;
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
        processListeners.add(settings);
        processListeners.add(checkbox);
        processListeners.add(versions);
        processListeners.add(accounts);
        stateListeners.add(buttons.play);
        add(messagePanel);
        add(del(0));
        add(accounts);
        add(versions);
        add(del(0));
        add(checkbox);
        add(del(0));
        add(buttons);
        tlauncher.getElyManager().addListener(this);
        tlauncher.getVersionManager().addListener(this);
        tlauncher.getDownloader().addListener(this);
    }

    private void runProcess() {
        LoginException error = null;
        boolean success = true;
        List force = processListeners;
        synchronized (processListeners) {
            Iterator var5 = processListeners.iterator();

            LoginForm.LoginProcessListener listener;
            while (var5.hasNext()) {
                listener = (LoginForm.LoginProcessListener) var5.next();

                try {
                    //log("Listener:", listener);
                    listener.logginingIn();
                } catch (LoginWaitException var9) {
                    LoginWaitException loginError = var9;
                    log("Catched a wait task from listener", listener);

                    try {
                        loginError.getWaitTask().runTask();
                    } catch (LoginException var8) {
                        log("Catched an error on a wait task.");
                        error = var8;
                    }
                } catch (LoginException var10) {
                    log("Catched an error on a listener", listener);
                    error = var10;
                }

                if (error != null) {
                    log(error);
                    success = false;
                    break;
                }
            }

            if (success) {
                var5 = processListeners.iterator();

                while (var5.hasNext()) {
                    listener = (LoginForm.LoginProcessListener) var5.next();
                    listener.loginSucceed();
                }
            } else {
                var5 = processListeners.iterator();

                while (var5.hasNext()) {
                    listener = (LoginForm.LoginProcessListener) var5.next();
                    listener.loginFailed();
                }
            }
        }

        if (error != null) {
            log("Login process has ended with an error.");
        } else {
            global.setForcefully("login.account", accounts.getAccount().getUsername(), false);
            global.setForcefully("login.account.type", accounts.getAccount().getType(), false);
            global.setForcefully("login.version", versions.getVersion().getID(), false);
            global.store();
            boolean force1 = checkbox.forceupdate.isSelected();
            changeState(LoginForm.LoginState.LAUNCHING);

            log("Calling Minecraft Launcher...");
            tlauncher.launch(this, server, force1);
            checkbox.forceupdate.setSelected(false);
        }
    }

    private void stopProcess() {
        while (!tlauncher.isLauncherWorking()) {
            U.sleepFor(500L);
        }

        changeState(LoginForm.LoginState.STOPPING);
        tlauncher.getLauncher().stop();
    }

    public void startLauncher() {
        startLauncher(null);
    }

    public void startLauncher(ServerList.Server server) {
        if (!Blocker.isBlocked(this)) {
            while (accounts.getAccount() != null && accounts.getAccount().getType() == Account.AccountType.ELY && tlauncher.getElyManager().isRefreshing()) {
                U.sleepFor(500L);
            }

            this.server = server;
            autologin.setActive(false);
            startThread.iterate();
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
            Iterator var3 = stateListeners.iterator();

            while (var3.hasNext()) {
                LoginForm.LoginStateListener listener = (LoginForm.LoginStateListener) var3.next();
                listener.loginStateChanged(state);
            }

        }
    }

    public void block(Object reason) {
        if (!Blocker.getBlockList(this).contains("refresh")) {
            Blocker.block(accounts, reason);
        }

        Blocker.block(reason, settings, versions, checkbox, buttons);
    }

    public synchronized void unblock(Object reason) {
        Blocker.unblock(reason, settings, accounts, versions, checkbox, buttons);
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

    public void onElyUpdating(ElyManager manager) {
        if (accounts.getAccount() != null && accounts.getAccount().getType() == Account.AccountType.ELY) {
            Blocker.block(buttons.play, "ely");
        }

        accounts.updateAccount();
        repaint();
    }

    public void onElyUpdated(ElyManager manager) {
        Blocker.unblock(buttons.play, "ely");
        repaint();
    }

    public void onAuthPassing(Authenticator auth) {
        Blocker.block(this, "auth");
    }

    public void onAuthPassingError(Authenticator auth, Throwable e) {
        Blocker.unblock(this, "auth");
        if (!(e.getCause() instanceof IOException) && !(e.getCause() instanceof RuntimeException)) {
            throw new LoginException("Cannot auth!");
        }
    }

    public void onAuthPassed(Authenticator auth) {
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
            tlauncher.getUpdater().asyncFindUpdate();
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
        manager.addListener(this);
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

    class LoginAbortedException extends Exception {
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
        void loginStateChanged(LoginForm.LoginState var1);
    }

    class StartThread extends LoopedThread {
        StartThread() {
            startAndWait();
        }

        protected void iterateOnce() {
            try {
                runProcess();
            } catch (Throwable var2) {
                Alert.showError(var2);
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
            } catch (Throwable var2) {
                Alert.showError(var2);
            }

        }
    }
}