package com.turikhay.tlauncher.ui;


import java.awt.GridLayout;

import javax.swing.JPanel;

public class SaveSettingsPanel extends JPanel {
	private static final long serialVersionUID = 4156489797984574935L;
	
	SaveSettingsPanel(SettingsForm sf){
		setOpaque(false);
		setLayout(new GridLayout(0, 2));
		
		add("Center", sf.saveButton);
		add("South", sf.defButton);
	}

}
