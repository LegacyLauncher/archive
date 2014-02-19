package com.turikhay.tlauncher.ui.scenes;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.animate.Animator;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.settings.SettingsPanel;

public class DefaultScene extends PseudoScene {
	private static final long serialVersionUID = -1460877989848190921L;
	
	final int
		LOGINFORM_WIDTH, LOGINFORM_HEIGHT, SETTINGSFORM_WIDTH, SETTINGSFORM_HEIGHT, MARGIN;
	
	public final LoginForm loginForm;
	public final SettingsPanel settingsForm;
	
	private boolean settings;

	public DefaultScene(MainPane main) {
		super(main);
		
		LOGINFORM_WIDTH = LOGINFORM_HEIGHT = 240;
		SETTINGSFORM_WIDTH = 500; SETTINGSFORM_HEIGHT = 475;
		MARGIN = 25; // between loginForm and settingForm
		
		
		this.settingsForm = new SettingsPanel(this);
		this.settingsForm.setSize(SETTINGSFORM_WIDTH, SETTINGSFORM_HEIGHT);
		this.add(settingsForm);
		
		this.loginForm = new LoginForm(this);
		this.loginForm.setSize(LOGINFORM_WIDTH, LOGINFORM_HEIGHT);
		this.add(loginForm);
		
		setSettings(false, false);
	}
	
	public void onResize(){
		super.onResize();
		
		setSettings(settings, false);
	}
	
	public void setSettings(boolean shown, boolean update){
		if(settings == shown && update) return;
		
		if(shown) settingsForm.unblock("");
		else settingsForm.block("");
		
		if(update)
			settingsForm.saveValues();
		
		int w = getWidth(), h = getHeight(), hw = w / 2, hh = h / 2;
		int lf_x, lf_y, sf_x, sf_y;
		
		if(shown){
			int bw = LOGINFORM_WIDTH + SETTINGSFORM_WIDTH + MARGIN, hbw = bw / 2; // bw = width of lf and sf.			
			
			lf_x = hw - hbw; lf_y =  hh - LOGINFORM_HEIGHT / 2;
			sf_x = hw - hbw + SETTINGSFORM_WIDTH / 2 + MARGIN; sf_y = hh - SETTINGSFORM_HEIGHT / 2;
		} else {
			lf_x = hw - LOGINFORM_WIDTH / 2; lf_y = hh - LOGINFORM_HEIGHT / 2;
			sf_x = w * 2; sf_y = hh - SETTINGSFORM_HEIGHT / 2;
		}
		
		Animator.move(loginForm, lf_x, lf_y);
		Animator.move(settingsForm, sf_x, sf_y);
		
		settings = shown;
	}
	public void setSettings(boolean shown){ setSettings(shown, true); }
	
	public void toggleSettings(){
		this.setSettings(!settings);
	}

	@Override
	public void block(Object reason) {
		Blocker.block(reason, loginForm, settingsForm);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblock(reason, loginForm, settingsForm);
	}

}
