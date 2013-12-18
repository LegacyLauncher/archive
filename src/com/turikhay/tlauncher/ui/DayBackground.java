package com.turikhay.tlauncher.ui;

import java.awt.Graphics2D;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.U;

public class DayBackground extends LightBackground implements AnimatedBackground {
	public final double MIN = -.5, MAX = 1.5;
	private boolean started, redraw;
	public DayBackground(DecoratedPanel comp, double loc) {
		super(comp, loc);
	}
	
	public void start(){
		if(started) return;
		
		TLauncher.getInstance().getSettings().set("gui.sun", true);
		started = true;
		redraw = true;
		
		AsyncThread.execute(new Runnable(){
			public void run(){
				while(started)
					tick();
			}
		});
		
		U.log("Sun started");
	}
	
	public void suspend(){
		started = false;
		
		U.log("Sun suspended");
	}
	
	public void stop(){
		TLauncher.getInstance().getSettings().set("gui.sun", false);
		started = false;
		
		U.log("Sun stopped");
	}
	
	public boolean isAllowed(){
		return TLauncher.getInstance().getSettings().getBoolean("gui.sun");
	}
	
	public void setAllowed(boolean b){
		TLauncher.getInstance().getSettings().set("gui.sun", b);
	}
	
	private void tick(){
		if(sunLocation <= MIN) sunLocation = MAX; else sunLocation -= 0.001;
		
		long start = System.nanoTime();
		comp.repaint();
		long end = System.nanoTime(), diff = end - start;
		
		if(diff > 1000000)
			U.log("Sun is probably lagging ("+diff+" ns > 1000000 ns).");
		
		U.sleepFor(100);
	}
	
	public void draw(Graphics2D g, boolean force) {
		if(redraw){ force = true; redraw = false; }
		super.draw(g, force);
	}
}
