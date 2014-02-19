package com.turikhay.tlauncher.ui.console;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.util.StringUtil;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;
import com.turikhay.util.logger.LinkedStringStream;
import com.turikhay.util.logger.Logger;
import com.turikhay.util.logger.PrintLogger;

public class Console implements Logger {
	private static List<ConsoleFrame> frames = Collections.synchronizedList(new ArrayList<ConsoleFrame>());
	
	private final Configuration global;
	private final ConsoleFrame frame;
	private final String name;
	
	private LinkedStringStream stream;
	private PrintLogger logger;
	
	private CloseAction close;
	private boolean killed;
	
	public Console(Configuration global, PrintLogger logger, String name, boolean show){
		this.global = global;		
		this.name = name;
		
		TLauncherFrame.initLookAndFeel();
		
		this.frame = new ConsoleFrame(this, global, name);
		frames.add(frame);
		
		this.update();
		
		frame.addWindowListener(new WindowListener(){
			public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) { save(); onClose(); }
			public void windowClosed(WindowEvent e) { }
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});
		
		frame.addComponentListener(new ComponentListener(){
			public void componentResized(ComponentEvent e) {
				save(false);
			}
			public void componentMoved(ComponentEvent e) {
				save(false);
			}
			public void componentShown(ComponentEvent e) {
				save(true);
			}
			public void componentHidden(ComponentEvent e) {
				save(true);
			}
	    });
		
		
		if(logger == null){
			this.logger = null;
			this.stream = new LinkedStringStream();
			this.stream.setLogger(this);
		} else {
			this.logger = logger; logger.setMirror(this);
			this.stream = logger.getStream();
		}
		
		if(show) show();
		
		if(stream.getLength() != 0)		
			rawlog(stream.getOutput());
	}
	
	public void log(String s) {		
		if(logger != null) logger.rawlog(s);
		else stream.write(s.toCharArray());
	}
	
	public void log(Object... o) {	
		log(U.toLog(o));
	}
	
	public void rawlog(String s){
		if(StringUtil.lastChar(s) == '\n')
			frame.print(s);
		else
			frame.println(s);
	}
	
	public void rawlog(Object...o){
		rawlog(U.toLog(o));
	}

	public void rawlog(char[] c) {
		rawlog(new String(c));
	}
	
	public PrintLogger getLogger(){
		return logger;
	}
	
	public String getOutput(){
		return stream.getOutput();
	}
	
	public void update(){ check();
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
		
		frame.setSize(width, height);
		frame.setLocation(x, y);
		
		SearchPrefs sf = frame.getSearchPrefs();
		sf.setCaseSensetive(mcase);
		sf.setWordSearch(whole);
		sf.setCycled(cycle);
		sf.setRegExp(regexp);
	}
	
	public void save(){
		save(false);
	}
	
	public void save(boolean flush){ check();
		if(global == null) return;
		
		String
			prefix = "gui.console.";
		int[] size = getSize(), position = getPosition();
		
		global.set(prefix + "width", size[0], false);
		global.set(prefix + "height", size[1], false);
		global.set(prefix + "x", position[0], false);
		global.set(prefix + "y", position[1], false);
		
			prefix += "search.";
		boolean[] prefs = frame.getSearchPrefs().get();
		
		global.set(prefix + "mcase", prefs[0], false);
		global.set(prefix + "whole", prefs[1], false);
		global.set(prefix + "cycle", prefs[2], false);
		global.set(prefix + "regexp", prefs[3], flush);		
	}
	
	private void check(){
		if(killed)
			throw new IllegalStateException("Console is already killed!");
	}
	
	public void setShown(boolean shown){
		if(shown) show(); else hide();
	}
	
	public void show(){
		show(true);
	}
	
	public void show(boolean toFront){ check();
		frame.setVisible(true);
		frame.scrollBottom();
		
		if(toFront) frame.toFront();
	}
	
	public void hide(){ check();
		frame.setVisible(false);
	}
	
	public void clear(){ check();
		frame.clear();
	}
	
	public void kill(){ check(); save();
		frame.setVisible(false); frame.clear();
		
		frames.remove(frame);
		killed = true;
	}

	public void killIn(long millis){ check(); save();
		frame.hideIn(millis);
	
		AsyncThread.execute(new Runnable(){
			public void run(){
				if(isHidden())
					kill();
			}
		}, millis + 1000);
	}

	public boolean isKilled(){ check();
		return killed;
	}
	
	public boolean isHidden(){ check();
		return !frame.isShowing();
	}
	
	public String getName(){
		return name;
	}

	public Point getPositionPoint(){ check();
		return frame.getLocation();
	}

	public int[] getPosition(){ check();
		Point p = this.getPositionPoint();
		return new int[]{p.x, p.y};
	}

	public Dimension getDimension(){ check();
		return frame.getSize();
	}

	public int[] getSize(){ check();
		Dimension d = this.getDimension();
		return new int[]{d.width, d.height};
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
	
	public static void updateLocale(){
		for(ConsoleFrame frame : frames)
			frame.updateLocale();
	}
	
	public enum CloseAction {
		KILL, EXIT;
	}
}
