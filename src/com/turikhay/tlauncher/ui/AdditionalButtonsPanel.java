package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class AdditionalButtonsPanel extends BlockablePanel {
	private static final long serialVersionUID = 7217356608637239309L;
	
	SupportButton support;
	FolderButton folder;
	RefreshButton refresh;
	SettingsButton settings;
	
	AdditionalButtonsPanel(ButtonPanel bp){
		LoginForm lf = bp.lf;
		
		LayoutManager layout = new GridLayout(0, 4);
		this.setLayout(layout);
		
		support = new SupportButton(lf);
		folder = new FolderButton(lf);
		refresh = new RefreshButton(lf);
		settings = new SettingsButton(lf);
		
		this.add(support);
		this.add(folder);
		this.add(refresh);
		this.add(settings);
	}

	protected void blockElement(Object reason) {
		refresh.setEnabled(false);
		settings.setEnabled(false);
	}

	protected void unblockElement(Object reason) {
		refresh.setEnabled(true);
		settings.setEnabled(true);
	}

}
