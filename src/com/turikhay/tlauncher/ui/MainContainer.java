package com.turikhay.tlauncher.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Panel;

public class MainContainer extends DecoratedPanel implements LocalizableComponent {
	private static final long serialVersionUID = 8925486339442046362L;
	
	TLauncherFrame f;
	
	final MainContainer instance = this;
	final DayBackground bg;
	
	Font font;
	
	MainContainer(TLauncherFrame f){
		this.f = f;
		this.font = f.getFont();
		this.bg = new DayBackground(this, -1, false);
		
		this.setBackground(f.bgcolor);
		this.setPanelBackground(bg);
		
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		
		this.add(f.lf);
		
		bg.start();
	}
	
	private void setContent(Panel pan){
		this.removeAll();
		
		this.add(pan);
		this.validate();
	}
	
	public void showLogin(){
		this.setContent(f.lf);
	}
	
	public void showSettings(){
		this.setContent(f.sf);
	}
	
	public void update(Graphics g){
		this.paint(g);
	}

	public void updateLocale() {		
		f.lf.updateLocale();
		f.sf.updateLocale();
	}
}
