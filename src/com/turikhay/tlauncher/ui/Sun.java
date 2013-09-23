package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import com.turikhay.tlauncher.timer.TimerTask;
import com.turikhay.tlauncher.util.U;

public class Sun {
	final TLauncherFrame f;
	final Sun sun = this;
	
	private Image image;
	private int width, height, x, y;
	private double percent;
	
	private Color bgcolor;
	
	private boolean	cancelled,
					sameY; /* this makes the sun stay in the same height on the screen,
							  becomes "true" when "percent" is near zero. */
	
	public Sun(MainContainer mc){
		this.f = mc.f;
		
		image = mc.f.sun;
		bgcolor = mc.f.bgcolor;
		
		width = image.getWidth(null);
		height = image.getHeight(null);
		
		percent = U.doubleRandom(); // Random double between 0 and 1
		if(percent < 0) percent += 0.5;
		
		if(allowed()) this.addTask();
	}
	
	private void addTask(){
		this.f.ti.add("sun", new TimerTask(){
			public void run(){
				percent = percent - 0.001;
				
				f.mc.repaint();
			}
			public boolean isRepeating() { return true; }
			public int getTicks() { return 1; }
		});
	}
	
	private void recalculateCoordinates(int w, int h){
		if(percent < -1) percent = 1;
		
		if(sameY) sameY = false;
		if(!sameY && percent < 0.1 && percent > -0.1) sameY = true;
		
		x = (int) (w * percent);
		
		if(!sameY){
			y = (int) (h / 2 - height / 2);
			if(percent > 0) y += percent * 100; else y -= percent * 100;
		}
			
		//U.log(x + " : " + y + " : " + percent);
	}
	
	public void onPaint(int w, int h, Graphics g){
		if(cancelled) return;
		
		this.recalculateCoordinates(w, h);
		
		g.drawImage(image, x, y, null);
		
		Color oldcolor = g.getColor();
		
		g.setColor(bgcolor);
		g.fillRect(x + width, y, width, height);
		
		g.setColor(oldcolor);
	}
	
	public void suspend(){
		U.log("The sun is suspended.");
		
		this.cancelled = true;
		f.ti.remove("sun");		
	}
	
	public void resume(){
		if(!allowed()) return;
		
		U.log("The sun is resumed.");
		this.cancelled = false;
		this.addTask();
	}
	
	public void cancel(){
		suspend();
		f.t.settings.set("gui.sun", false);
		
		U.log("The sun is stopped.");
	}
	
	public void allow(){
		f.t.settings.set("gui.sun", true);
		
		U.log("The sun is resumed.");
	}
	
	private boolean allowed(){
		return f.global.getBoolean("gui.sun");
	}
}
