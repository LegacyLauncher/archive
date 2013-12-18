package com.turikhay.tlauncher.ui;

import java.awt.Graphics;

import javax.swing.JLayeredPane;

import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;

public class MainPane extends JLayeredPane implements MinecraftLauncherListener {
	private static final long serialVersionUID = 8925486339442046362L;
	
	TLauncherFrame f;
	
	final Integer
		BACKGROUND_PANEL, LOGINFORM, SETTINGSFORM, PROFILECREATOR; 
	
	final MainPane instance = this;
	
	final DecoratedPanel bgpan;
	final Background bg;
	final LoginForm lf;
	final SettingsForm sf;
	final ProfileCreatorForm spcf;
	
	private boolean settings;
	
	MainPane(TLauncherFrame f){
		this.f = f;
		this.lf = f.lf;
		this.sf = f.sf;
		this.spcf = f.spcf;
		
		int i = 0;
		BACKGROUND_PANEL = ++i;
		LOGINFORM = ++i;
		SETTINGSFORM = ++i;
		PROFILECREATOR = ++i;
		
		this.bgpan = new DecoratedPanel(); this.bg = chooseBackground();
		this.bgpan.setPanelBackground(bg);
		
		this.add(this.bgpan, BACKGROUND_PANEL);
		this.add(this.lf, LOGINFORM);
		this.add(this.sf, SETTINGSFORM);
		//this.add(this.spcf, PROFILECREATOR);
		
		this.startBackground();
	}
	
	public void onResize(){
		this.bgpan.setBounds(0, 0, getWidth(), getHeight());
		
		this.lf.setSize(250, 250);
		this.sf.setSize(500, 500);
		
		//int hw = getWidth() / 2, hh = getHeight() / 2;
		//this.spcf.setBounds(hw - 110, hh - 110, 220, 220);
		
		this.setSettings(settings, false);
	}
	
	private Background chooseBackground(){
		return (f.global.getBoolean("gui.sun")? new DayBackground(bgpan, -1) : new LightBackground(bgpan, -1));
	}
	
	public boolean startBackground(){
		if(!(bg instanceof AnimatedBackground)) return false;
		
		AnimatedBackground abg = (AnimatedBackground) bg;
		if(!abg.isAllowed()) return false;
		
		abg.start();
		return true;
	}
	
	public boolean stopBackground(){
		if(!(bg instanceof AnimatedBackground)) return false;
		
		((AnimatedBackground) bg).stop();
		return true;
	}
	
	public boolean suspendBackground(){
		if(!(bg instanceof AnimatedBackground)) return false;
		
		((AnimatedBackground) bg).suspend();
		return true;
	}
	
	public void setSettings(boolean shown, boolean animate){
		if(settings == shown && animate) return;
		
		if(shown) sf.unblockElement("");
		else sf.blockElement("");
		
		sf.updateIfSaveable();
		
		int w = getWidth(), h = getHeight(), hw = w / 2, hh = h / 2;
		int lf_x, lf_y, sf_x, sf_y;
		
		if(shown){
			int margin = 15, bw = 500 + 250 + margin, hbw = bw / 2; // bw = width of lf and sf.			
			
			lf_x = hw - hbw; lf_y =  hh - 125;
			sf_x = hw - hbw + 250 + margin; sf_y = hh - 250;
		} else {
			lf_x = hw - 125; lf_y = hh - 125;
			sf_x = w; sf_y = hh - 250;
		}
		
		AnimateThread.animate(lf, lf_x, lf_y);
		AnimateThread.animate(sf, sf_x, sf_y);
		
		settings = shown;
	}
	public void setSettings(boolean shown){ setSettings(shown, true); }
	
	public void toggleSettings(){
		this.setSettings(!settings);
	}
	
	public void update(Graphics g){
		this.paint(g);
	}

	public void onMinecraftCheck() {}
	public void onMinecraftPrepare() {}
	public void onMinecraftWarning(String langpath, Object replace) {}
	
	public void onMinecraftLaunch() { stopBackground(); }
	public void onMinecraftLaunchStop() { startBackground(); }
	public void onMinecraftClose() { startBackground(); }
	public void onMinecraftError(MinecraftLauncherException knownError) { startBackground(); }
	public void onMinecraftError(Throwable unknownError) { startBackground(); }
	public void onMinecraftCrash(Crash crash) { startBackground(); }
}
