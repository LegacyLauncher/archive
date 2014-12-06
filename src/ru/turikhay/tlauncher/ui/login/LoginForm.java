package ru.turikhay.tlauncher.ui.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.DownloaderListener;
import ru.turikhay.tlauncher.managers.ServerList.Server;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class LoginForm extends CenterPanel implements
MinecraftListener, AuthenticatorListener, VersionManagerListener, DownloaderListener {
	// listeners
	private final List<LoginStateListener> stateListeners = Collections.synchronizedList(new ArrayList<LoginStateListener>());
	private final List<LoginProcessListener> processListeners = Collections.synchronizedList(new ArrayList<LoginProcessListener>());

	// input vars
	public final DefaultScene scene;
	public final MainPane pane;
	private final SettingsPanel settings;

	// output vars
	public final AccountComboBox accounts;
	public final VersionComboBox versions;
	public final CheckBoxPanel checkbox;
	public final ButtonPanel buttons;
	public final AutoLogin autologin;

	// internal vars
	private final StartThread startThread;
	private final StopThread stopThread;
	private LoginState state = LoginState.STOPPED;

	private Server server;


	public LoginForm(DefaultScene scene) {
		this.scene = scene;
		this.pane = scene.getMainPane();

		this.settings = scene.settingsForm;

		this.startThread = new StartThread();
		this.stopThread = new StopThread();

		this.autologin = new AutoLogin(this);

		this.accounts = new AccountComboBox(this);
		this.versions = new VersionComboBox(this);
		this.checkbox = new CheckBoxPanel(this);
		this.buttons = new ButtonPanel(this);

		processListeners.add(autologin);
		processListeners.add(settings);
		processListeners.add(checkbox);
		processListeners.add(versions);
		processListeners.add(accounts);

		stateListeners.add(buttons.play);

		add(messagePanel);
		add(del(Del.CENTER));
		add(accounts);
		add(versions);
		add(del(Del.CENTER));
		add(checkbox);
		add(del(Del.CENTER));
		add(buttons);

		tlauncher.getVersionManager().addListener(this);
		tlauncher.getDownloader().addListener(this);
	}

	private void runProcess() {
		LoginException error = null;
		boolean success = true;

		synchronized (processListeners) {
			for (LoginProcessListener listener : processListeners) {

				try {
					log("Listener:", listener);
					listener.logginingIn();
				} catch (LoginWaitException wait) {
					log("Catched a wait task from this listener, waiting...");
					try {
						wait.getWaitTask().runTask();
					} catch (LoginException waitError) {
						log("Catched an error on a wait task.");
						error = waitError;
					}
				} catch (LoginException loginError) {
					log("Catched an error on a listener");
					error = loginError;
				}

				if (error == null)
					continue;

				log(error);

				success = false;
				break;
			}

			if(success)
				for (LoginProcessListener listener : processListeners)
					listener.loginSucceed();
			else
				for (LoginProcessListener listener : processListeners)
					listener.loginFailed();
		}

		if (error != null) {
			log("Login process has ended with an error.");
			return;
		}

		global.setForcefully("login.account", accounts.getAccount().getUsername(), false);
		global.setForcefully("login.version", versions.getVersion().getID(), false);
		global.store();

		log("Login was OK. Trying to launch now.");


		boolean force = checkbox.forceupdate.isSelected();
		changeState(LoginState.LAUNCHING);
		tlauncher.launch(this, server, force);

		checkbox.forceupdate.setSelected(false);
	}

	private void stopProcess() {
		while(!tlauncher.isLauncherWorking()) {
			log("waiting for launcher");
			U.sleepFor(500);
		}

		changeState(LoginState.STOPPING);
		tlauncher.getLauncher().stop();
	}

	public void startLauncher() {
		startLauncher(null);
	}

	public void startLauncher(Server server) {
		if (Blocker.isBlocked(this))
			return;

		this.server = server;

		autologin.setActive(false);
		startThread.iterate();
	}

	public void stopLauncher() {
		stopThread.iterate();
	}

	class StartThread extends LoopedThread {

		StartThread() {
			startAndWait();
		}

		@Override
		protected void iterateOnce() {
			try {
				runProcess();
			} catch(Throwable t) {
				Alert.showError(t);
			}
		}
	}

	class StopThread extends LoopedThread {

		StopThread() {
			startAndWait();
		}

		@Override
		protected void iterateOnce() {
			try {
				stopProcess();
			} catch(Throwable t) {
				Alert.showError(t);
			}
		}
	}

	private void changeState(LoginState state) {
		if(state == null)
			throw new NullPointerException();

		if(this.state == state)
			return;

		this.state = state;

		for(LoginStateListener listener : stateListeners)
			listener.loginStateChanged(state);
	}

	@Override
	public void block(Object reason) {
		if (!Blocker.getBlockList(this).contains(REFRESH_BLOCK))
			Blocker.block(accounts, reason);

		Blocker.block(reason, settings, versions, checkbox, buttons);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblock(reason, settings, accounts, versions, checkbox, buttons);
	}

	@Override
	public void onDownloaderStart(Downloader d, int files) {
		Blocker.block(this, DOWNLOADER_BLOCK);
	}

	@Override
	public void onDownloaderAbort(Downloader d) {
		Blocker.unblock(this, DOWNLOADER_BLOCK);
	}

	@Override
	public void onDownloaderProgress(Downloader d, double progress, double speed) {
	}

	@Override
	public void onDownloaderFileComplete(Downloader d, Downloadable file) {
	}

	@Override
	public void onDownloaderComplete(Downloader d) {
		Blocker.unblock(this, DOWNLOADER_BLOCK);
	}

	@Override
	public void onVersionsRefreshing(VersionManager manager) {
		Blocker.block(this, REFRESH_BLOCK);
	}

	@Override
	public void onVersionsRefreshingFailed(VersionManager manager) {
		Blocker.unblock(this, REFRESH_BLOCK);
	}

	@Override
	public void onVersionsRefreshed(VersionManager manager) {
		Blocker.unblock(this, REFRESH_BLOCK);
	}

	@Override
	public void onAuthPassing(Authenticator auth) {
		Blocker.block(this, AUTH_BLOCK);
	}

	@Override
	public void onAuthPassingError(Authenticator auth, Throwable e) {
		Blocker.unblock(this, AUTH_BLOCK);

		Throwable cause = e.getCause();
		if (cause != null && e.getCause() instanceof IOException)
			return;

		throw new LoginException("Cannot auth!");
	}

	@Override
	public void onAuthPassed(Authenticator auth) {
		Blocker.unblock(this, AUTH_BLOCK);
	}

	@Override
	public void onMinecraftPrepare() {
		Blocker.block(this, LAUNCH_BLOCK);
	}

	@Override
	public void onMinecraftAbort() {
		Blocker.unblock(this, LAUNCH_BLOCK);
	}

	@Override
	public void onMinecraftLaunch() {
		changeState(LoginState.LAUNCHED);
	}

	@Override
	public void onMinecraftClose() {
		Blocker.unblock(this, LAUNCH_BLOCK);
		changeState(LoginState.STOPPED);

		tlauncher.getVersionManager().startRefresh(true);
	}

	@Override
	public void onMinecraftError(Throwable e) {
		Blocker.unblock(this, LAUNCH_BLOCK);
		changeState(LoginState.STOPPED);
	}

	@Override
	public void onMinecraftKnownError(MinecraftException e) {
		Blocker.unblock(this, LAUNCH_BLOCK);
		changeState(LoginState.STOPPED);
	}

	@Override
	public void onMinecraftCrash(Crash crash) {
		Blocker.unblock(this, LAUNCH_BLOCK);
		changeState(LoginState.STOPPED);
	}

	public static final String
	LOGIN_BLOCK = "login",
	REFRESH_BLOCK = "refresh",
	LAUNCH_BLOCK = "launch",
	AUTH_BLOCK = "auth",
	UPDATER_BLOCK = "update",
	DOWNLOADER_BLOCK = "download";

	public enum LoginState {
		LAUNCHING, STOPPING, STOPPED, LAUNCHED;
	}

	public static interface LoginStateListener {
		void loginStateChanged(LoginState state);
	}

	public static interface LoginProcessListener {
		void logginingIn() throws LoginException;
		void loginFailed();
		void loginSucceed();
	}

	public static abstract class LoginListener implements LoginProcessListener {
		@Override
		public abstract void logginingIn() throws LoginException;

		@Override
		public void loginFailed() {
		}

		@Override
		public void loginSucceed() {
		}
	}

	class LoginAbortedException extends Exception {
	}
}
