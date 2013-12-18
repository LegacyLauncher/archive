package com.turikhay.tlauncher.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class SettingsPanel extends JPanel {
	private static final long serialVersionUID = 4212962090384406608L;
	
	final SettingsForm sf;
	final int keyWidth = 150, valueWidth = 150;
	
	public SettingsPanel(SettingsForm settingsform){
		this.sf = settingsform;
	}
	
	void createInterface(){
		setOpaque(false);
		setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = 17;

		constraints.gridy = 0;
		constraints.weightx = 0.0D;
		constraints.fill = 2;
		add(sf.cdel(Del.CENTER, keyWidth, 5), constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.cdel(Del.CENTER, valueWidth, 5), constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.gameDirCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.gameDirField, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.resolutionCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.resolutionField, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 2;
		add(sf.cdel(Del.BOTTOM, keyWidth, 5), constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.cdel(Del.BOTTOM, valueWidth, 5), constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.versionChoice, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.versionsPan, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 2;
		add(sf.cdel(Del.CENTER, keyWidth, 5), constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.cdel(Del.CENTER, valueWidth, 5), constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.pathCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.pathCustomField, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.argsCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.argsPan, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 2;
		add(sf.cdel(Del.BOTTOM, keyWidth, 5), constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.cdel(Del.BOTTOM, valueWidth, 5), constraints);
		
		/*++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.memoryCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		//add(sf.memoryCustomField, constraints);*/
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.tlauncherSettings, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.tlauncherPan, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.autologinCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.autologinField, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.launchActionCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.launchActionChoice, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.connTimeoutLabel, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.connectionField, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 0;
		add(sf.langCustom, constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.langChoice, constraints);
		
		++constraints.gridy;
		constraints.weightx = 0.0D;
		constraints.fill = 2;
		add(sf.cdel(Del.BOTTOM, keyWidth, 5), constraints);
		constraints.fill = 2;
		constraints.weightx = 1.0D;
		add(sf.cdel(Del.BOTTOM, valueWidth, 5), constraints);
	}
}
