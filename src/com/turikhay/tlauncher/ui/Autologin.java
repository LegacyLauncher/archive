package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.AsyncThread;

public class Autologin {
	public final static int DEFAULT_TIMEOUT = 3;
	public final static int MAX_TIMEOUT = 10;
	
	private final LoginForm lf;
	
	public final int timeout;
	boolean enabled, active;
	
	private Runnable task;
	private int sec;
	
	Autologin(LoginForm loginform, boolean enabled, int timeout){
		this.lf = loginform;
		this.enabled = enabled; this.timeout = sec = timeout;
		
		task = new Runnable(){
			public void run() {
				while(sec > 0){
					sleepFor(1000);
					
					if(updateLogin())
						callLogin();
				}
			}
		};
	}
	
	public void startLogin(){
		active = true;
		AsyncThread.execute(task);	
	}
	
	public void stopLogin(){
		sec = -1;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	private boolean updateLogin(){ --sec;
		lf.buttons.cancel.setLabel("loginform.cancel", "t", this.sec);
		if(sec != 0) return false;
		
		stopLogin();		
		return true;
	}
	
	private void callLogin(){ lf.callLogin(); }
	private void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){}
	}



}
