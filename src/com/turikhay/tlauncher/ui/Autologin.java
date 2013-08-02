package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.timer.TimerTask;

public class Autologin implements TimerTask {
	private LoginForm lf;
	
	boolean cancelled;
	private boolean last;
	private int timeout, elapsed = -1;
	
	public Autologin(LoginForm lf, int timeout){
		this.lf = lf;
		this.timeout = timeout;
	}
	
	
	public void run() { ++elapsed;
		if(cancelled || elapsed > timeout) return;
		
		lf.setAutologinRemaining(timeout-elapsed);
		
		if(timeout == elapsed) last = true;
		else if(!last) return;
		
		lf.callAutoLogin();
	}
	public boolean isRepeating() { return !last; }
	public int getTicks() { return 1; }

}
