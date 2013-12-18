package com.turikhay.tlauncher.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;

import net.minecraft.launcher.updater.VersionSyncInfo;

public class CheckBoxPanel extends BlockablePanel implements LoginListener {
	private static final long serialVersionUID = 1808335203922301270L;
	
	private final LoginForm lf;
	
	//Checkbox premiumloginbox;
	LocalizableCheckbox autologinbox;
	
	LocalizableCheckbox forceupdatebox;
	private boolean forceupdate;
	
	CheckBoxPanel(LoginForm loginform, boolean autologin_enabled, boolean console_enabled){
		this.lf = loginform;
		
		BoxLayout lm = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(lm);
		this.setOpaque(false);
		
		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setAlignmentY(CENTER_ALIGNMENT);
		
		/*premiumloginbox = new LocalizableCheckbox("loginform.checkbox.premium", premium_enabled);
		premiumloginbox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean newstate = e.getStateChange() == ItemEvent.SELECTED;
				
				if(newstate) lf.maininput.add(lf.maininput.password);
				else lf.maininput.remove(lf.maininput.password);
				lf.f.mc.validate();
			}
		});*/
		
		autologinbox = new LocalizableCheckbox("loginform.checkbox.autologin", autologin_enabled);
		autologinbox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean newstate = e.getStateChange() == ItemEvent.SELECTED;
				
				lf.setAutoLogin(newstate);
				lf.defocus();
			}
		});
		
		forceupdatebox = new LocalizableCheckbox("loginform.checkbox.forceupdate");
		forceupdatebox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean newstate = e.getStateChange() == ItemEvent.SELECTED;
				
				forceupdate = newstate;
				onForceUpdateChanged();
				lf.defocus();
			}
		});
		
		this.add(autologinbox);
		this.add(Box.createHorizontalGlue());
		this.add(forceupdatebox);
		//this.add(premiumloginbox);
	}
	
	public void setForceUpdate(boolean s){ this.forceupdate = s; this.onForceUpdateChanged(); }
	public boolean getForceUpdate(){ return this.forceupdate; }
	
	private void onForceUpdateChanged(){
		lf.buttons.updateEnterButton();
		forceupdatebox.setState(forceupdate);
	}
	
	void uncheckAutologin(){
		autologinbox.setState(false);
	}

	protected void blockElement(Object reason) {
		this.setEnabled(false);
	}
	protected void unblockElement(Object reason) {
		this.setEnabled(true);
	}

	public boolean onLogin() {
		VersionSyncInfo syncInfo = lf.versionchoice.getSyncVersionInfo();
		
		if(syncInfo == null) return true; // Will be caught in the next listener
		
		boolean
			supporting = syncInfo.isOnRemote(),
			installed = syncInfo.isInstalled();
	
		if(getForceUpdate())
			if(!supporting) Alert.showWarning("forceupdate.onlylibraries");
			else{ if(installed && !Alert.showQuestion("forceupdate.question", true)) return false; }
		
		return true;
	}
	public void onLoginFailed() {}
	public void onLoginSuccess() {
		this.setForceUpdate(false);
	}
}
