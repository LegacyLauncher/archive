package com.turikhay.tlauncher.ui;

import com.turikhay.util.AsyncThread;

public class Autologin implements LoginListener {
	public final static int DEFAULT_TIMEOUT = 3;
	public final static int MAX_TIMEOUT = 10;
	
	private final LoginForm lf;
	
	public final int timeout;
	boolean enabled, active;
	
	private Runnable task;
	private int sec;
	
	Autologin(LoginForm loginform, boolean enabled, int timeout){
		if(timeout < 2) timeout = 2;
		
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
	
	public void cancel(){
		enabled = false;
		stopLogin();
		
		lf.checkbox.uncheckAutologin();
		lf.buttons.toggleSouthButton();
		if(active) lf.versionchoice.asyncRefresh();
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

	public boolean onLogin() {
		if(!enabled) return true;
		
		stopLogin();
		active = false;
		lf.buttons.toggleSouthButton();
		
		return true;
	}
	public void onLoginFailed() {}
	public void onLoginSuccess() {}
}
