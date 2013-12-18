package com.turikhay.tlauncher.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.VersionManager;

public class RefreshButton extends ImageButton implements RefreshedListener {
	private static final long serialVersionUID = -1334187593288746348L;
	public final static int TYPE_REFRESH = 0, TYPE_CANCEL = 1;
	private boolean VERSIONS, RESOURCES;
	
	private LoginForm lf;
	private VersionManager vm;
	private int type;
	private final Image
		refresh = loadImage("refresh.png"),
		cancel = loadImage("cancel.png");
	
	RefreshButton(LoginForm loginform, int type){
		this.lf = loginform;
		this.vm = lf.t.getVersionManager();
		
		this.rotation = ImageRotation.CENTER;
		this.setType(type, false);
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { onPressButton(); }
		});
		
		this.initImage();
	}
	RefreshButton(LoginForm loginform){ this(loginform, TYPE_REFRESH); }
	
	private void onPressButton(){
		
		switch(type){
		case TYPE_REFRESH:
			vm.asyncRefresh();
			break;
		case TYPE_CANCEL:
			vm.cancelVersionRefresh();
			break;
		default:
			throw new IllegalArgumentException("Unknown type: "+type+". Use RefreshButton.TYPE_* constants.");
		}
		
		lf.defocus();
	}
	
	public void setType(int type){ this.setType(type, true); }
	public void setType(int type, boolean repaint){	
		switch(type){
		case TYPE_REFRESH:
			if(VERSIONS || RESOURCES) return; // Something is refreshing so far.
			
			this.image = refresh;
			break;
		case TYPE_CANCEL:
			this.image = cancel;
			break;
		default:
			throw new IllegalArgumentException("Unknown type: "+type+". Use RefreshButton.TYPE_* constants.");
		}
		
		this.type = type;
		if(repaint && getGraphics() != null){ this.paint(getGraphics()); }
	}
	public void onVersionManagerUpdated(VersionManager vm) {
		this.vm = vm;
	}
	public void onVersionsRefreshing(VersionManager vm) {
		VERSIONS = true;
		
		this.setType(TYPE_CANCEL);
	}
	public void onVersionsRefreshingFailed(VersionManager vm) {
		VERSIONS = false;
		
		this.setType(TYPE_REFRESH);
	}
	public void onVersionsRefreshed(VersionManager vm) {
		VERSIONS = false;
		
		this.setType(TYPE_REFRESH);
	}
	public void onResourcesRefreshing(VersionManager vm) {
		RESOURCES = true;
		
		this.setType(TYPE_CANCEL);
	}
	public void onResourcesRefreshed(VersionManager vm) {
		RESOURCES = false;
		
		this.setType(TYPE_REFRESH);
	}

}
