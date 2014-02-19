package com.turikhay.tlauncher.ui.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.managers.VersionManager;
import com.turikhay.tlauncher.component.managers.VersionManagerListener;
import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.settings.SettingsPanel;
import com.turikhay.tlauncher.ui.swing.Del;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.util.U;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener, AuthenticatorListener, VersionManagerListener, UpdaterListener, UpdateListener {
	private static final long serialVersionUID = -7539492515708852727L;
	public static final String LOGIN_BLOCK = "login", REFRESH_BLOCK = "refresh", LAUNCH_BLOCK = "launch", AUTH_BLOCK = "auth", UPDATER_BLOCK = "update";
	
	final List<LoginListener> listeners = new ArrayList<LoginListener>();
	final LoginThread thread;
	
	public final DefaultScene scene;
	final LoginForm instance;
	final SettingsPanel settings;
	final MainPane pane;
	
	public final AccountComboBox accounts;
	public final VersionComboBox versions;
	public final CheckBoxPanel checkbox;
	public final ButtonPanel buttons;
	
	public final AutoLogin autologin;
	
	public LoginForm(DefaultScene scene){
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
	
	private void saveValues(){
		log("Saving values...");
		
		global.set("login.account", accounts.getAccount().getUsername(), false);
		global.set("login.version", versions.getVersion().getID(), false);
		global.store();
		
		log("Values has been saved!");
	}
	
	public void callLogin(){
		if(Blocker.isBlocked(this)){
			log("Cannot call login, UI is blocked by:", Blocker.getBlockList(this));
			return;
		}
		
		autologin.setActive(false);
		thread.start();
	}
	
	private void runLogin(){
		log("Running login process from a thread");
		
		LoginException error = null;
		boolean success = true;
		
		synchronized(listeners){
			for(LoginListener listener : listeners){
				log("Running on a listener", listener.getClass().getSimpleName());
				
				try{
					listener.onLogin();
				}
				catch(LoginWaitException wait){
					log("Catched a wait task from this listener, waiting...");
					try{
						wait.getWaitTask().runTask();
					}
					catch(LoginException waitError){
						log("Catched an error on a wait task.");
						error = waitError;
					}
				}
				catch(LoginException loginError){
					log("Catched an error on a listener");
					error = loginError;
				}
				
				if(error == null) continue;
				
				log(error);
				success = false;
				break;
			}
			
			for(LoginListener listener : listeners){
				if(success)
					listener.onLoginSuccess();
				else
					listener.onLoginFailed();
			}
		}
		
		if(error == null)
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
		if(!reason.equals(REFRESH_BLOCK)) Blocker.block(accounts, reason);
		Blocker.block(reason, settings, versions, checkbox, buttons);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblock(reason, settings, accounts, versions, checkbox, buttons);
	}
	
	/// START LISTENER BLOCK
	
	/// AUTHENTICATION
	
	@Override
	public void onAuthPassing(Authenticator auth) {
		Blocker.block(this, AUTH_BLOCK);
	}

	@Override
	public void onAuthPassingError(Authenticator auth, Throwable e) {
		Blocker.unblock(this, AUTH_BLOCK);
		
		Throwable cause = e.getCause();
		if(cause != null && e.getCause() instanceof IOException) return;
		
		throw new LoginException("Cannot auth!");
	}

	@Override
	public void onAuthPassed(Authenticator auth) {
		Blocker.unblock(this, AUTH_BLOCK);
	}
	
	/// VERSION REFRESH

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
	
	/// MINECRAFT LAUNCHER
	
	@Override
	public void onMinecraftCheck() {
		Blocker.block(this, LAUNCH_BLOCK);
	}
	@Override
	public void onMinecraftPrepare() {}

	@Override
	public void onMinecraftLaunch() {
		Blocker.unblock(this, LAUNCH_BLOCK);
		
		tlauncher.hide();
		tlauncher.getVersionManager().asyncRefresh();
		tlauncher.getUpdater().asyncFindUpdate();
	}

	@Override
	public void onMinecraftLaunchStop() {
		Blocker.unblock(this, LAUNCH_BLOCK);
	}

	@Override
	public void onMinecraftClose() {
		tlauncher.show();
		tlauncher.getUpdaterListener().applyDelayedUpdate();
	}

	@Override
	public void onMinecraftKnownError(MinecraftLauncherException knownError) {
		Alert.showError(lang.get("launcher.error.title"), lang.get(knownError.getLangpath()), knownError.getReplace());
		
		Blocker.unblock(this, LAUNCH_BLOCK);
		tlauncher.show();
	}

	@Override
	public void onMinecraftError(Throwable unknownError) {
		Alert.showError(lang.get("launcher.error.title"), lang.get("launcher.error.unknown"), unknownError);
		
		Blocker.unblock(this, LAUNCH_BLOCK);
		tlauncher.show();
	}

	@Override
	public void onMinecraftWarning(String langpath, Object replace) {
		Alert.showAsyncWarning(lang.get("launcher.warning.title"), lang.get("launcher.warning." + langpath, replace));
	}

	@Override
	public void onMinecraftCrash(Crash crash) {		
		Crash.handle(crash);
	}
	
	/// UPDATER
	
	@Override
	public void onUpdateError(Update u, Throwable e) {
		Blocker.unblock(this, UPDATER_BLOCK);
	}

	@Override
	public void onUpdateDownloading(Update u) {
		Blocker.block(this, UPDATER_BLOCK);
	}

	@Override
	public void onUpdateDownloadError(Update u, Throwable e) {
		Blocker.unblock(this, UPDATER_BLOCK);
	}

	@Override
	public void onUpdateReady(Update u) {}
	@Override
	public void onUpdateApplying(Update u) {}
	@Override
	public void onUpdateApplyError(Update u, Throwable e) {}
	@Override
	public void onUpdaterRequesting(Updater u) {}
	@Override
	public void onUpdaterRequestError(Updater u) {}

	@Override
	public void onUpdateFound(Update upd) {		
		if(!Updater.isAutomode()) return;
		
		upd.addListener(this);
		Blocker.block(this, UPDATER_BLOCK);
	}

	@Override
	public void onUpdaterNotFoundUpdate(Updater u) {}
	@Override
	public void onAdFound(Updater u, Ad ad) {}
	
	/// END LISTENER BLOCK
	
	class LoginThread extends Thread {
		private final LoginForm loginForm;
		private boolean isStarted, isRunning;
		
		LoginThread(LoginForm loginForm){
			this.loginForm = loginForm;
		}
		
		@Override
		public void start(){
			if(isRunning){
				log("LoginThread is runned before and hasn't been stopped yet.");
				return;
			}
						
			this.isRunning = true;
			if(isStarted) return;
			
			this.isStarted = true;
			super.start();
		}
		
		@Override
		public void run(){
			Blocker.block(loginForm, LOGIN_BLOCK);
			
			try{ loginForm.runLogin(); }
			catch(Throwable e){
				Alert.showError(e, false);
			}
			
			Blocker.unblock(loginForm, LOGIN_BLOCK);
			
			this.isRunning = false;
			while(!isRunning) U.sleepFor(100);
			this.run();
		}
	}
}
