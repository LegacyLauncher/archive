package com.turikhay.tlauncher.ui.login.buttons;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.managers.ComponentManager;
import com.turikhay.tlauncher.component.managers.ComponentManagerListener;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.swing.ImageButton;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;

public class RefreshButton extends ImageButton implements Blockable, ComponentManagerListener, UpdaterListener {
	private static final long serialVersionUID = -1334187593288746348L;
	public final static int TYPE_REFRESH = 0, TYPE_CANCEL = 1;
	
	private LoginForm lf;
	private int type;
	private final Image
		refresh = loadImage("refresh.png"),
		cancel = loadImage("cancel.png");
	private Updater updaterFlag;
	
	RefreshButton(LoginForm loginform, int type){
		this.lf = loginform;
		
		this.rotation = ImageRotation.CENTER;
		this.setType(type, false);
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { onPressButton(); }
		});
		
		this.initImage();
		
		TLauncher.getInstance().getManager().addListener(this);
		TLauncher.getInstance().getUpdater().addListener(this);
	}
	RefreshButton(LoginForm loginform){ this(loginform, TYPE_REFRESH); }
	
	private void onPressButton(){
		switch(type){
		case TYPE_REFRESH:
			if(updaterFlag != null) updaterFlag.asyncFindUpdate();
			TLauncher.getInstance().getManager().startAsyncRefresh();
			break;
		case TYPE_CANCEL:
			TLauncher.getInstance().getManager().stopRefresh();
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
	
	@Override
	public void onUpdaterRequesting(Updater u) {}
	@Override
	public void onUpdaterRequestError(Updater u) {
		this.updaterFlag = u;
	}
	@Override
	public void onUpdateFound(Update upd) {
		this.updaterFlag = null;
	}
	@Override
	public void onUpdaterNotFoundUpdate(Updater u) {
		this.updaterFlag = null;
	}
	public void onAdFound(Updater u, Ad ad) {}
	
	@Override
	public void onComponentsRefreshing(ComponentManager manager) {
		Blocker.block(this, LoginForm.REFRESH_BLOCK);
	}
	@Override
	public void onComponentsRefreshed(ComponentManager manager) {
		Blocker.unblock(this, LoginForm.REFRESH_BLOCK);
	}
	
	//
	
	@Override
	public void block(Object reason) {
		if(reason.equals(LoginForm.REFRESH_BLOCK)) setType(TYPE_CANCEL);
		else setEnabled(false);
	}
	@Override
	public void unblock(Object reason) {
		if(reason.equals(LoginForm.REFRESH_BLOCK)) setType(TYPE_REFRESH);
		setEnabled(true);
	}
}
