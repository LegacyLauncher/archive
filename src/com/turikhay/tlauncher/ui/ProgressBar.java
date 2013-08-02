package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.font.LineMetrics;

import javax.swing.JProgressBar;

import com.turikhay.tlauncher.util.U;

public class ProgressBar extends JProgressBar {
	private static final long serialVersionUID = 5163801043869691008L;
	
	private String center_string, west_string, east_string;
	private boolean started = false, wait = false,
		center_string_changed = true, west_string_changed = true, east_string_changed = true;
	private int
		center_tw, center_th, center_x, center_y,
		west_th, west_x, west_y,
		east_tw, east_th, east_x, east_y,
		oldw, border = 10;
	
	ProgressBar(TLauncherFrame f){		
		this.setMinimum(0);
		this.setMaximum(100);
		
		this.setPreferredSize(new Dimension(f.getWidth(), 20));
		
		this.paintBorder = false;
		
		this.setVisible(false);
	}
	
	public void progressStart(){
		while(wait) sleepFor(10);
		
		this.setVisible(true);
		this.setValue(0);
		
		started = true;
	}
	
	public void progressStop(){
		while(wait) sleepFor(10);
		
		this.setWestString(null);
		this.setCenterString(null);
		this.setEastString(null);
		
		this.setVisible(false);
		
		started = false;
	}
	
	public boolean isStarted(){ return started; }
	
	public void setCenterString(String str){ str = U.r(str, 70);
		while(wait) sleepFor(10);
		this.center_string_changed = (center_string != str);
		
		this.center_string = str;
		this.repaint();
	}
	
	public String getCenterString(){
		return this.center_string;
	}
	
	public void setWestString(String str){ str = U.r(str, 50);
		while(wait) sleepFor(10);
		this.west_string_changed = (west_string != str);
		
		this.west_string = str;
		this.repaint();
	}
	
	public String getWestString(){
		return this.west_string;
	}
	
	public void setEastString(String str){ str = U.r(str, 50);
		while(wait) sleepFor(10);
		this.east_string_changed = (east_string != str);
		
		this.east_string = str;
		this.repaint();
	}
	
	public String getEastString(){
		return this.east_string;
	}
	
	public void update(Graphics g){
		super.update(g);
		
		this.paint(g);
	}
	
	public void paint(Graphics g){
		super.paint(g);
		
		boolean center = center_string != null, west = west_string != null, east = east_string != null, force = false;
		
		if(!(center || west || east)) return;
		
		wait = true;
		
		FontMetrics fm = g.getFontMetrics();
		int w = getWidth();
		
		if(oldw != w) force = true;
		
		if(center)
		if(force || center_string_changed){
			LineMetrics lm = fm.getLineMetrics(center_string, g);
			
			center_tw = fm.stringWidth(center_string); center_th = (int) lm.getHeight();
			
			center_x = (w / 2) - (center_tw / 2);
			center_y = center_th;
			
			this.center_string_changed = false;
		}
		
		if(west)
		if(force || west_string_changed){
			LineMetrics lm = fm.getLineMetrics(west_string, g);
			
			west_th = (int) lm.getHeight();
			
			west_x = border;
			west_y = west_th;
			
			this.west_string_changed = false;
		}
		
		if(east)
		if(force || east_string_changed){
			LineMetrics lm = fm.getLineMetrics(east_string, g);
			
			east_tw = fm.stringWidth(east_string); east_th = (int) lm.getHeight();
			
			east_x = w - east_tw - border;
			east_y = east_th;
			
			this.east_string_changed = false;
		}
		
		Color oldcolor = g.getColor();
		g.setColor(Color.black);
		
		if(center) g.drawString(center_string, center_x, center_y);
		if(west) g.drawString(west_string, west_x, west_y);
		if(east) g.drawString(east_string, east_x, east_y);
		
		oldw = w;
		
		g.setColor(oldcolor);
		
		wait = false;
	}
	
	private void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){}
	}

}
