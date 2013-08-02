package com.turikhay.tlauncher.handlers;

import com.turikhay.tlauncher.ui.Alert;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static ExceptionHandler instance;
	public static ExceptionHandler getInstance(){ return instance; }
	
	public ExceptionHandler(){
		super();
		if(instance != null) throw new IllegalStateException("Use method ExceptionHandler.getInstance() instead of creating new instance.");
		instance = this;
	}
	
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();	
		try{ Alert.showError("Exception in thread "+t.getName(), e); }
		catch(Exception w){ System.exit(2);	}
	}
}
