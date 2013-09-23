package com.turikhay.tlauncher.ui;

import java.awt.Checkbox;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;

public class CheckBoxPanel extends BlockablePanel implements LoginListener {
	private static final long serialVersionUID = 1808335203922301270L;
	
	private final LoginForm lf;
	
	Checkbox autologinbox;
	
	Checkbox forceupdatebox;
	private boolean forceupdate;
	
	CheckBoxPanel(LoginForm loginform, boolean autologin_enabled, boolean console_enabled){
		this.lf = loginform;
		
		LayoutManager lm = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(lm);
		
		autologinbox = new LocalizableCheckbox("loginform.checkbox.autologin");
		autologinbox.setState(autologin_enabled);
		autologinbox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean newstate = e.getStateChange() == ItemEvent.SELECTED;
				
				lf.setAutoLogin(newstate);
			}
		});
		
		forceupdatebox = new LocalizableCheckbox("loginform.checkbox.forceupdate");
		forceupdatebox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean newstate = e.getStateChange() == ItemEvent.SELECTED;
				
				forceupdate = newstate;
				onForceUpdateChanged();
			}
		});
		
		this.add(autologinbox);
		this.add(forceupdatebox);
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

	public void onLogin() {
		forceupdate = false;
		onForceUpdateChanged();
	}
	public void onLoginFailed() {}
	public void onLoginSuccess() {}
}
