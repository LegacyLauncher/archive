package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class AdditionalButtonsPanel extends BlockablePanel {
	private static final long serialVersionUID = 7217356608637239309L;
	
	public final SupportButton support;
	public final FolderButton folder;
	public final RefreshButton refresh;
	public final SettingsButton settings;
	
	private final LoginForm lf;
	
	AdditionalButtonsPanel(ButtonPanel bp){
		lf = bp.lf;
		
		LayoutManager layout = new GridLayout(0, 4);
		this.setLayout(layout);
		this.setOpaque(false);
		
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
		if(!reason.toString().endsWith("_refresh")) refresh.setEnabled(false);
		settings.setEnabled(false);
	}

	protected void unblockElement(Object reason) {
		if(!reason.toString().endsWith("_refresh")) refresh.setEnabled(true);
		settings.setEnabled(true);
	}
}
