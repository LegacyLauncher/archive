package com.turikhay.tlauncher.ui;

import java.awt.Graphics2D;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.U;

public class DayBackground extends LightBackground {
	public final double MIN = -.3, MAX = 1.3;
	private boolean started;
	public DayBackground(DecoratedPanel comp, double loc, boolean start) {
		super(comp, loc);
		
		if(start) start();
	}
	
	public void start(){
		TLauncher.getInstance().getSettings().set("gui.sun", true);
		started = true;
		
		AsyncThread.execute(new Runnable(){
			public void run(){
				while(started)
					tick();
			}
		});
	}
	
	public void suspend(){
		started = false;
	}
	
	public void stop(){
		TLauncher.getInstance().getSettings().set("gui.sun", false);
		started = false;
	}
	
	public boolean getState(){
		return TLauncher.getInstance().getSettings().getBoolean("gui.sun");
	}
	
	private void tick(){
		if(sunLocation <= MIN) sunLocation = MAX; else sunLocation -= 0.001;
		
		long start = U.n();
		comp.repaint();
		long end = U.n(), diff = Math.abs(end - start);
		
		U.log("Sun has drawn in", diff, "ns. Location:", sunLocation);
		U.sleepFor(1000);
	}
	
	public void draw(Graphics2D g, boolean force) {
		//g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) sunLocation));
		super.draw(g, force);
	}
}
