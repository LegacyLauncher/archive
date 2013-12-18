package com.turikhay.tlauncher.ui;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.CrashSignature;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.util.U;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener, AuthenticatorListener, UpdaterListener, UpdateListener {
	private static final long serialVersionUID = 6768252827144456302L;
	final String LAUNCH_BLOCK = "launch", AUTH_BLOCK = "auth", UPDATER_BLOCK = "update";
	
	final LoginForm instance = this;
	final SettingsForm settings;
	final List<LoginListener> listeners = new ArrayList<LoginListener>();
	
	final MainPane pane;
	
	public final MainInputPanel maininput;
	//public final ProfileChoicePanel profilechoice;
	public final VersionChoicePanel versionchoice;
	public final CheckBoxPanel checkbox;
	public final ButtonPanel buttons;
	
	public final Autologin autologin;
	
	LoginForm(TLauncherFrame fd){
		super(fd);
		
		settings = f.sf;
		pane = f.mp;
		
		String
			username = s.nget("login.username"),
			version = s.nget("login.version");
		boolean
			auto = s.getBoolean("login.auto"),
			console = s.getBoolean("login.debug");
		int
			timeout = s.getInteger("login.auto.timeout");
		
		this.autologin = new Autologin(this, auto, timeout);
		
		this.maininput = new MainInputPanel(this, username);
		//this.profilechoice = new ProfileChoicePanel(this);
		this.versionchoice = new VersionChoicePanel(this, version);
		this.checkbox = new CheckBoxPanel(this, auto, console);
		this.buttons = new ButtonPanel(this);
		
		this.addListener(autologin);
		this.addListener(checkbox);
		this.addListener(maininput);
		this.addListener(settings);
		this.addListener(versionchoice);
		
		this.add(error);
		this.add(maininput);
		this.add(versionchoice);
		this.add(del(Del.BOTTOM));
		this.add(checkbox);
		this.add(del(Del.CENTER));
		this.add(buttons);
	}
	
	private void save(){
		U.log("Saving login settings...");
		
		s.set("login.username", maininput.field.username);
		s.set("login.version", versionchoice.version);
		
		U.log("Login settings saved!");
	}
	
	public void callLogin(){ defocus();
		if(isBlocked()) return;
		
		U.log("Loggining in...");		
		if(!listenerOnLogin()){ U.log("Login cancelled"); return; }
		
		save();
		t.launch(this, checkbox.getForceUpdate());
		block(LAUNCH_BLOCK);
	}
	
	public void cancelLogin(){ defocus();
		U.log("cancellogin");
		
		unblock(LAUNCH_BLOCK);
	}
	
	void setAutoLogin(boolean enabled){
		if(!enabled) autologin.cancel();
		else {
			Alert.showAsyncMessage("loginform.checkbox.autologin.tip", l.get("loginform.checkbox.autologin.tip.arg"));
			autologin.enabled = true;
		}
		
		s.set("login.auto", autologin.enabled);
	}
	
	private void addListener(LoginListener ll){
		this.listeners.add(ll);
	}
	
	private boolean listenerOnLogin(){
		for(LoginListener ll : this.listeners){
			U.log("onLogin: ", ll.getClass().getSimpleName());
			if(!ll.onLogin())
				return false;
		}
		return true;
	}
	
	private void listenerOnFail(){
		for(LoginListener ll : this.listeners)
			ll.onLoginFailed();
	}
	
	private void listenerOnSuccess(){
		for(LoginListener ll : this.listeners)
			ll.onLoginSuccess();
	}
	
	protected void blockElement(Object reason){ defocus();	
		maininput.blockElement(reason);
		versionchoice.blockElement(reason);
		checkbox.blockElement(reason);
		buttons.blockElement(reason);
		
		settings.blockElement(reason);
	}
	protected void unblockElement(Object reason){ defocus();		
		maininput.unblockElement(reason);
		versionchoice.unblockElement(reason);
		checkbox.unblockElement(reason);
		buttons.unblockElement(reason);
		
		settings.unblockElement(reason);
	}
	
	public void onMinecraftCheck() {
		block(LAUNCH_BLOCK);
	}
	public void onMinecraftPrepare() {}
	public void onMinecraftLaunch() { unblock(LAUNCH_BLOCK);
		listenerOnSuccess();
		
		t.hide();
		versionchoice.asyncRefresh();
	}
	public void onMinecraftLaunchStop() { handleError(); }
	public void onMinecraftClose() { unblock(LAUNCH_BLOCK);
		t.show();
		
		if(autologin.enabled) t.getUpdater().asyncFindUpdate();
	}
	public void onMinecraftError(Throwable e) {
		Alert.showError(l.get("launcher.error.title"), l.get("launcher.error.unknown"), e);
		handleError();
	}
	public void onMinecraftError(String message) {
		Alert.showError(l.get("launcher.error.title"), l.get(message));
		handleError();
	}
	public void onMinecraftError(MinecraftLauncherException knownError) {
		Alert.showError(l.get("launcher.error.title"), l.get(knownError.getLangpath()), knownError.getReplace());
		handleError();
	}
	private void handleError(){ unblock(LAUNCH_BLOCK);
		listenerOnFail();
		
		t.show();
	}
	public void onMinecraftWarning(String langpath, Object replace){
		Alert.showWarning(l.get("launcher.warning.title"), l.get("launcher.warning." + langpath, "r", replace));		
	}
	public void updateLocale(){
		super.updateLocale();

		TLauncherFrame.updateContainer(this, true);
	}
	
	public void onMinecraftCrash(Crash crash) {
		String p = "crash.", title = l.get(p + "title");
		
		if(!crash.isRecognized()){
			Alert.showError(title, l.get(p + "unknown"), null);
			return;
		}
		
		String report = crash.getFile();
		
		for(CrashSignature sign : crash.getSignatures()){
			String path = sign.path, message = l.get(p + path), url = l.get(p + path + ".url");
			URI uri = U.makeURI(url);
			
			if(uri != null){
				if(Alert.showQuestion(title, message, report, false))
					try{ OperatingSystem.openLink(uri); }finally{}
			} else
				Alert.showMessage(title, message, report);
		}
		
		if(report == null) return;
		
		if(Alert.showQuestion(p + "store", false)){
			U.log("Removing crash report...");
			
			File file = new File(report);
			if(!file.exists())
				U.log("File is already removed. LOL.");
			else {
				try{
					
					if(!file.delete())
						throw new Exception("file.delete() returned false");
					
				}catch(Exception e){
					U.log("Can't delete crash report file. Okay.");
					Alert.showAsyncMessage(p + "store.failed", e);
					
					return;
				}
				U.log("Yay, crash report file doesn't exist by now.");
			}
			Alert.showAsyncMessage(p + "store.success");
		}
	}

	public void onAuthPassing(Authenticator auth) {
		block(AUTH_BLOCK);
	}

	public void onAuthPassingError(Authenticator auth, Throwable e) { unblock(AUTH_BLOCK);
		
	}

	public void onAuthPassed(Authenticator auth) { unblock(AUTH_BLOCK);
	}
	
	public void onUpdateError(Update u, Throwable e) {
		unblock(UPDATER_BLOCK);
	}
	public void onUpdateDownloading(Update u) {
		block(UPDATER_BLOCK);
	}
	public void onUpdateDownloadError(Update u, Throwable e) {
		unblock(UPDATER_BLOCK);
	}
	public void onUpdateReady(Update u) {}
	public void onUpdateApplying(Update u) {}
	public void onUpdateApplyError(Update u, Throwable e) {}
	public void onUpdaterRequesting(Updater u) {}
	public void onUpdaterRequestError(Updater u) {}
	public void onUpdateFound(Updater u, Update upd) {
		if(!Updater.isAutomode()) return;
		
		upd.addListener(this);
		block(UPDATER_BLOCK);
	}
	public void onUpdaterNotFoundUpdate(Updater u) {}
	public void onAdFound(Updater u, Ad ad) {}
}
