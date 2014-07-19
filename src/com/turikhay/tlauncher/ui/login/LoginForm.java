package com.turikhay.tlauncher.ui.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.managers.VersionManager;
import com.turikhay.tlauncher.managers.VersionManagerListener;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.minecraft.crash.Crash;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.settings.SettingsPanel;
import com.turikhay.tlauncher.ui.swing.Del;
import com.turikhay.util.async.ExtendedThread;

public class LoginForm extends CenterPanel implements MinecraftListener,
		AuthenticatorListener, VersionManagerListener {
	private static final long serialVersionUID = -7539492515708852727L;
	public static final String LOGIN_BLOCK = "login";
	public static final String REFRESH_BLOCK = "refresh";
	private static final String LAUNCH_BLOCK = "launch";
	private static final String AUTH_BLOCK = "auth";
	public static final String UPDATER_BLOCK = "update";

	private final List<LoginListener> listeners = new ArrayList<LoginListener>();
	private final LoginThread thread;

	public final DefaultScene scene;
	private final LoginForm instance;
	private final SettingsPanel settings;
	final MainPane pane;

	public final AccountComboBox accounts;
	public final VersionComboBox versions;
	public final CheckBoxPanel checkbox;
	public final ButtonPanel buttons;

	public final AutoLogin autologin;

	public LoginForm(DefaultScene scene) {
		this.instance = this;

		this.scene = scene;

		this.settings = scene.settingsForm;
		this.pane = scene.getMainPane();

		this.thread = new LoginThread(this);

		this.autologin = new AutoLogin(this);

		this.accounts = new AccountComboBox(this);
		this.versions = new VersionComboBox(this);
		this.checkbox = new CheckBoxPanel(this);
		this.buttons = new ButtonPanel(this);

		listeners.add(autologin);
		listeners.add(settings);
		listeners.add(checkbox);
		listeners.add(versions);
		listeners.add(accounts);

		add(messagePanel);
		add(del(Del.CENTER));
		add(accounts);
		add(versions);
		add(del(Del.CENTER));
		add(checkbox);
		add(del(Del.CENTER));
		add(buttons);

		TLauncher.getInstance().getVersionManager().addListener(this);
	}

	private void saveValues() {
		log("Saving values...");

		global.setForcefully("login.account", accounts.getAccount().getUsername(), false);
		global.setForcefully("login.version", versions.getVersion().getID(), false);
		global.store();

		log("Values has been saved!");
	}

	public void callLogin() {
		if (Blocker.isBlocked(this)) {
			log("Cannot call login, UI is blocked by:",
					Blocker.getBlockList(this));
			return;
		}

		autologin.setActive(false);
		thread.start();
	}

	private void runLogin() {
		log("Running login process from a thread");

		LoginException error = null;
		boolean success = true;

		synchronized (listeners) {
			for (LoginListener listener : listeners) {
				log("Running on a listener", listener.getClass()
						.getSimpleName());

				try {
					listener.onLogin();
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

			for (LoginListener listener : listeners) {
				if (success)
					listener.onLoginSuccess();
				else
					listener.onLoginFailed();
			}
		}

		if (error == null)
			log("Login process is OK :)");
		else {
			log("Login process has ended with an error.");
			return;
		}

		saveValues();

		boolean force = checkbox.forceupdate.isSelected();
		tlauncher.launch(instance, force);

		checkbox.forceupdate.setSelected(false);
	}

	@Override
	public void block(Object reason) {
		if (!reason.equals(REFRESH_BLOCK))
			Blocker.block(accounts, reason);
		Blocker.block(reason, settings, versions, checkbox, buttons);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblock(reason, settings, accounts, versions, checkbox, buttons);
	}

	// / START LISTENER BLOCK

	// / AUTHENTICATION

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

	// / VERSION REFRESH

	@Override
	public void onVersionsRefreshing(VersionManager vm) {
		Blocker.block(this, REFRESH_BLOCK);
	}

	@Override
	public void onVersionsRefreshingFailed(VersionManager vm) {
		Blocker.unblock(this, REFRESH_BLOCK);
	}

	@Override
	public void onVersionsRefreshed(VersionManager vm) {
		Blocker.unblock(this, REFRESH_BLOCK);
	}

	// / MINECRAFT LAUNCHER

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
	}

	@Override
	public void onMinecraftClose() {
		Blocker.unblock(this, LAUNCH_BLOCK);
	}

	@Override
	public void onMinecraftKnownError(MinecraftException knownError) {
		Blocker.unblock(this, LAUNCH_BLOCK);
	}

	@Override
	public void onMinecraftError(Throwable unknownError) {
		Blocker.unblock(this, LAUNCH_BLOCK);
	}

	@Override
	public void onMinecraftCrash(Crash crash) {
		Blocker.unblock(this, LAUNCH_BLOCK);
	}

	// / END LISTENER BLOCK

	class LoginThread extends ExtendedThread {
		private final LoginForm loginForm;

		LoginThread(LoginForm loginForm) {
			super("LoginThread");

			this.loginForm = loginForm;
			super.start();
		}

		@Override
		public void start() {
			unblockThread(LAUNCH_BLOCK);
		}

		@Override
		public void run() {
			while (true) {
				blockThread(LAUNCH_BLOCK);

				try {
					loginForm.runLogin();
				} catch (Throwable e) {
					Alert.showError(e);
				}
			}
		}
	}
}
