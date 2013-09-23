package com.turikhay.tlauncher.util;

import java.awt.Dimension;
import java.awt.Point;

import com.turikhay.tlauncher.ui.ConsoleFrame;

public class Console {
	//private final String name;
	private final ConsoleFrame cf;
	private final Console instance = this;
	private String del = null;
	private boolean killed;
	
	public Console(String name){
		this.cf = new ConsoleFrame(name);
	}
	
	public Console(String name, boolean show){
		this(name);		
		cf.setVisible(show);
	}
	
	public void show(){ check();
		cf.setVisible(true);
		cf.toFront(); cf.scrollBottom();
	}
	
	public void hide(){ check();
		cf.setVisible(false);
	}
	
	public void kill(){ check();
		cf.setVisible(false);
		killed = true;
	}
	
	public void killIn(long millis){
		AsyncThread.execute(new Runnable(){
			public void run(){
				instance.kill();
			}
		}, millis);
	}
	
	public boolean isKilled(){ check();
		return killed;
	}
	
	public boolean isHidden(){ check();
		return !cf.isShowing();
	}
	
	public void log(Object obj){ check();
		cf.print(del + obj + "");
	}
	
	public void log(Object obj0, Object obj1){ check();
		cf.print(del + obj0 + " " + obj1);
	}
	
	public void log(Object obj, Throwable e){ check();
		cf.println(del + obj + "");
		cf.print(U.stackTrace(e));
	}
	
	public void log(Throwable e){ check();
		cf.print(del + U.stackTrace(e));
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
	
	private void check(){
		if(killed)
			throw new IllegalStateException("Console is already killed!");
		
		if(del == null) del = "";
		else if(del == "") del = "\n";
	}
}
