package com.turikhay.tlauncher.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.U;

public class Console {
	private static List<ConsoleFrame> frames = new ArrayList<ConsoleFrame>();
	private final GlobalSettings global;
	//private final String name;
	private final ConsoleFrame cf;
	
	private String del = null;
	private boolean killed;
	
	private CloseAction close;
	
	public Console(String name){ this((TLauncher.getInstance() != null)? TLauncher.getInstance().getSettings() : null, name); }
	
	public Console(GlobalSettings global, String name){
		this.cf = new ConsoleFrame(this, global, name);
		this.global = global;
		
		this.cf.addWindowListener(new WindowListener(){
			public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) { save(); onClose(); }
			public void windowClosed(WindowEvent e) { }
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});
		
		frames.add(cf);
		
		if(global == null) return;
		
		String
			prefix = "gui.console.";
		int
			width = global.getInteger(prefix + "width", ConsoleFrame.minWidth),
			height = global.getInteger(prefix + "height", ConsoleFrame.minHeight),
			x = global.getInteger(prefix + "x", 0),
			y = global.getInteger(prefix + "y", 0);
		//
			prefix += "search.";
		boolean
			mcase = global.getBoolean(prefix + "mcase"),
			whole = global.getBoolean(prefix + "whole"),
			cycle = global.getBoolean(prefix + "cycle"),
			regexp = global.getBoolean(prefix + "regexp");
		
		this.cf.setSize(width, height);
		this.cf.setLocation(x, y);
		
		SearchPrefs sf = this.cf.getSearchPrefs();
		sf.setCaseSensetive(mcase);
		sf.setWordSearch(whole);
		sf.setCycled(cycle);
		sf.setRegExp(regexp);
	}
	
	public Console(GlobalSettings global, String name, boolean show){
		this(global, name);		
		cf.setVisible(show);
	}
	public Console(String name, boolean show){ this((TLauncher.getInstance() != null)? TLauncher.getInstance().getSettings() : null, name, show); }
	
	public void show(){ check();
		cf.setVisible(true); cf.toFront();
		cf.scrollBottom();
	}
	
	public void hide(){ check();
		cf.setVisible(false);
	}
	
	public void clear(){ check();
		cf.clear();		
	}
	
	public void kill(){ check(); save();
		cf.setVisible(false); cf.clear();
		frames.remove(cf);
		killed = true;
	}
	
	public void killIn(long millis){ check(); save();
		cf.hideIn(millis);
		
		AsyncThread.execute(new Runnable(){
			public void run(){
				if(!cf.isVisible())
					kill();
			}
		}, millis + 1000);
	}
	
	public boolean isKilled(){ check();
		return killed;
	}
	
	public boolean isHidden(){ check();
		return !cf.isShowing();
	}
	
	public void log(Object... obj){ check();
		cf.print(del);
		cf.print(U.toLog(obj));
	}
	
	public Point getPositionPoint(){ check();
		return cf.getLocation();
	}
	
	public int[] getPosition(){ check();
		Point p = this.getPositionPoint();
		return new int[]{p.x, p.y};
	}
	
	public Dimension getDimension(){ check();
		return cf.getSize();
	}
	
	public int[] getSize(){ check();
		Dimension d = this.getDimension();
		return new int[]{d.width, d.height};
	}
	
	public String getOutput(){
		return cf.getOutput();
	}
	
	public void save(){ check();
		if(global == null) return;
		
		String
			prefix = "gui.console.";
		int[] size = getSize(), position = getPosition();
		
		global.set(prefix + "width", size[0], false);
		global.set(prefix + "height", size[1], false);
		global.set(prefix + "x", position[0], false);
		global.set(prefix + "y", position[1], false);
		
			prefix += "search.";
		boolean[] prefs = this.cf.getSearchPrefs().get();
		
		global.set(prefix + "mcase", prefs[0], false);
		global.set(prefix + "whole", prefs[1], false);
		global.set(prefix + "cycle", prefs[2], false);
		global.set(prefix + "regexp", prefs[3], true);
	}
	
	public CloseAction getCloseAction(){
		return this.close;
	}
	
	public void setCloseAction(CloseAction action){
		this.close = action;
	}
	
	private void onClose(){
		if(close == null) return;
		
		switch(close){
		case KILL: kill();
		case EXIT: TLauncher.kill();
		}
	}
	
	private void check(){
		if(killed)
			throw new IllegalStateException("Console is already killed!");
		
		if(del == null) del = "";
		else if(del == "") del = "\n";
	}
	
	public static void updateLocale(){
		for(ConsoleFrame frame : frames)
			frame.updateLocale();
	}
	
	public enum CloseAction {
		KILL, EXIT;
	}
}
