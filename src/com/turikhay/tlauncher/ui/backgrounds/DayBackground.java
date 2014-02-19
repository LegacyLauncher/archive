package com.turikhay.tlauncher.ui.backgrounds;

import java.awt.Graphics2D;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;

public class DayBackground extends LightBackground {
	private static final long serialVersionUID = -3722426754002999260L;
	
	public final double MIN = -.5, MAX = 1.5;
	private final int TICK = 100;
	
	private boolean started, redraw;
	
	public DayBackground(MainPane main) {
		this(main, U.doubleRandom());
	}
	
	public DayBackground(MainPane main, double loc) {
		super(main, loc);
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
		
		log("Sun has been started");
	}
	
	public void suspend(){
		started = false;
		
		log("Sun has been suspended");
	}
	
	public void stop(){
		TLauncher.getInstance().getSettings().set("gui.sun", false);
		started = false;
		
		log("Sun has been stopped");
	}
	
	private void tick(){
		if(!isVisible()) return;
		if(sunLocation <= MIN) sunLocation = MAX; else sunLocation -= 0.001;
		
		long start = System.nanoTime();
		repaint();
		long end = System.nanoTime(), diff = end - start;
		
		if(diff > 1000000)
			log("Sun is probably lagging ("+diff+" ns > 1000000 ns).");
		
		U.sleepFor(TICK);
	}
	
	public void draw(Graphics2D g, boolean force) {
		if(redraw){ force = true; redraw = false; }
		super.draw(g, force);
	}
	
	private static void log(Object...o){ U.log("[DayBackground]", o); }
}
