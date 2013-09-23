package com.turikhay.tlauncher.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.launcher_.updater.VersionSyncInfo;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.util.U;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener {
	private static final long serialVersionUID = 6768252827144456302L;
	
	final LoginForm instance = this;
	final SettingsForm settings;
	final List<LoginListener> listeners = new ArrayList<LoginListener>();
	
	public final MainInputPanel maininput;
	public final VersionChoicePanel versionchoice;
	public final CheckBoxPanel checkbox;
	public final ButtonPanel buttons;
	
	public final Autologin autologin;
	
	LoginForm(TLauncherFrame fd){
		super(fd);
		
		settings = f.sf;
		
		String
			username = s.get("login.username"),
			version = s.get("login.version");
		boolean
			autologin = s.getBoolean("login.auto"),
			console = s.getBoolean("login.debug");
		int
			timeout = s.getInteger("login.auto.timeout");
		
		this.autologin = new Autologin(this, autologin, timeout);
		
		this.maininput = new MainInputPanel(this, username);
		this.versionchoice = new VersionChoicePanel(this, version);
		this.checkbox = new CheckBoxPanel(this, autologin, console);
		this.buttons = new ButtonPanel(this);
		
		this.add(error);
		this.add(maininput);
		this.add(versionchoice);
		this.add(del(Del.BOTTOM));
		this.add(checkbox);
		this.add(del(Del.TOP));
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
		U.log("login");
		postAutoLogin();
		
		if(!maininput.field.check(false)) return;
		if(!settings.save()){ f.mc.showSettings(); return; }
		if(!versionchoice.foundlocal){
			block("refresh");
			versionchoice.refresh();
			unblock("refresh");
			if(!versionchoice.foundlocal) setError(l.get("versions.notfound"));
			
			return;
		}
		else setError(null);
		
		VersionSyncInfo syncInfo = versionchoice.getSyncVersionInfo();
				
		boolean
			supporting = syncInfo.isOnRemote(),
			installed = syncInfo.isInstalled();
		
		if(checkbox.getForceUpdate())
			if(!supporting) Alert.showWarning("forceupdate.onlylibraries");
			else{ if(installed && !Alert.showQuestion("forceupdate.question", true)) return; }
		
		save(); listenerOnLogin();
		t.launch(this, checkbox.getForceUpdate());
	}
	
	public void cancelLogin(){ defocus();
		U.log("cancellogin");
		
		unblock("launcher");
	}
	
	void setAutoLogin(boolean enabled){
		if(!enabled) cancelAutoLogin();
		else {
			Alert.showMessage("loginform.checkbox.autologin.tip", l.get("loginform.checkbox.autologin.tip.arg"));
			autologin.enabled = true;
		}
		
		s.set("login.auto", autologin.enabled);
	}
	
	private void cancelAutoLogin(){
		autologin.enabled = false;
		
		autologin.stopLogin();
		
		checkbox.uncheckAutologin();
		buttons.toggleSouthButton();
		if(autologin.active) versionchoice.asyncRefresh();
	}
	
	private void postAutoLogin(){
		if(!autologin.enabled) return;
		
		autologin.stopLogin(); autologin.active = false;
		buttons.toggleSouthButton();
	}
	
	public void addListener(LoginListener ll){
		this.listeners.add(ll);
	}
	
	public void removeListener(LoginListener ll){
		this.listeners.remove(ll);
	}
	
	private void listenerOnLogin(){
		for(LoginListener ll : this.listeners)
			ll.onLogin();
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
	}
	protected void unblockElement(Object reason){ defocus();		
		maininput.unblockElement(reason);
		versionchoice.unblockElement(reason);
		checkbox.unblockElement(reason);
		buttons.unblockElement(reason);
	}
	
	public void onMinecraftCheck() {
		block("launcher");
	}
	public void onMinecraftPrepare() {
		block("launcher");
		
		f.mc.bg.suspend();
	}
	public void onMinecraftLaunch() { unblock("launcher"); listenerOnSuccess();
		versionchoice.asyncRefresh();
	}
	public void onMinecraftClose() { unblock("launcher");
		f.mc.bg.start();
	}
	public void onMinecraftError(Throwable e) { handleError();
		Alert.showError(l.get("launcher.error.title"), l.get("launcher.error.unknown"), e);
	}
	public void onMinecraftError(String message) { handleError();
		Alert.showError(l.get("launcher.error.title"), l.get(message));
	}
	public void onMinecraftError(MinecraftLauncherException knownError) { handleError();
		Alert.showError(l.get("launcher.error.title"), l.get(knownError.getLangpath()), knownError.getReplace());
	}
	private void handleError(){
		listenerOnFail();
		unblock("launcher");
		
		if(f.mc.bg.getState()) f.mc.bg.start();
	}
	public void onMinecraftWarning(String langpath, Object replace){
		Alert.showWarning(l.get("launcher.warning.title"), l.get("launcher.warning." + langpath, "r", replace));		
	}
	public void updateLocale(){
		super.updateLocale();

		TLauncherFrame.updateContainer(this, true);
	}
}
