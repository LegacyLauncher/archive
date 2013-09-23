package com.turikhay.tlauncher.ui;

import javax.swing.BoxLayout;

public class TLauncherSettingsPanel extends BlockablePanel {
	private static final long serialVersionUID = -9108973380914818944L;
	
	TLauncherSettingsPanel(SettingsForm sf){
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		add(sf.consoleSelect);
		add(sf.sunSelect);
	}
	
	protected void blockElement(Object reason) {
		this.setEnabled(false);
	}

	protected void unblockElement(Object reason) {
		this.setEnabled(true);
	}
}
