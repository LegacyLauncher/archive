package com.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.turikhay.tlauncher.settings.Settings;

public class ButtonPanel extends BlockablePanel {
	private static final long serialVersionUID = 5873050319650201358L;
	
	public final static int ENTERBUTTON_INSTALL = -1;
	public final static int ENTERBUTTON_PLAY = 0;
	public final static int ENTERBUTTON_REINSTALL = 1;
	
	final LoginForm lf;
	final Settings l;

	public final LocalizableButton cancel, enter;
	public final AdditionalButtonsPanel addbuttons;
	
	
	ButtonPanel(LoginForm loginform){
		this.lf = loginform;
		this.l = lf.l;
		
		BorderLayout lm = new BorderLayout();
		lm.setVgap(2);
		lm.setHgap(1);
		this.setLayout(lm);
		this.setOpaque(false);
		
		enter = new LocalizableButton("loginform.enter");
		enter.setFont(lf.font_bold.deriveFont(lf.font_bold.getSize2D() + 5F));
		enter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { lf.callLogin(); }
		});
		
		cancel = new LocalizableButton("loginform.cancel", "t", lf.autologin.timeout);
		cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { lf.setAutoLogin(false); }
		});
		
		addbuttons = new AdditionalButtonsPanel(this);
		
		this.add("Center", enter);
		
		if(lf.autologin.enabled)
			this.add("South", cancel);
		else
			this.add("South", addbuttons);
	}
	
	void updateEnterButton(){
		if(lf.versionchoice.selected == null) return;
		
		boolean play = lf.versionchoice.selected.isInstalled(), force = lf.checkbox.getForceUpdate();
		int status = -2;
		
		if(play)
			if(force) status = ButtonPanel.ENTERBUTTON_REINSTALL; else status = ButtonPanel.ENTERBUTTON_PLAY;
		else status = ButtonPanel.ENTERBUTTON_INSTALL;
		
		String s = ".";
		switch(status){
		case ENTERBUTTON_INSTALL:
			s += "install"; break;
		case ENTERBUTTON_PLAY:
			s = ""; break;
		case ENTERBUTTON_REINSTALL:
			s += "reinstall"; break;
		default:
			throw new IllegalArgumentException("Status is invalid! Use ButtonPanel.ENTERBUTTON_* variables.");
		}
		enter.setLabel("loginform.enter" + s);
	}
	
	void toggleSouthButton(){
		remove(cancel);
		add("South", addbuttons);
		validate();
	}

	protected void blockElement(Object reason){
		enter.setEnabled(false);
		addbuttons.blockElement(reason);
	}
	protected void unblockElement(Object reason) {
		enter.setEnabled(true);
		addbuttons.unblockElement(reason);
	}
}
