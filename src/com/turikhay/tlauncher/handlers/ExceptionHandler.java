package com.turikhay.tlauncher.handlers;

import com.turikhay.tlauncher.ui.alert.Alert;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static ExceptionHandler instance;
	public static ExceptionHandler getInstance(){
		if(instance == null) instance = new ExceptionHandler();
		return instance;
	}
	
	private ExceptionHandler(){}
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();	
		try{ Alert.showError("Exception in thread "+t.getName(), e); }
		catch(Exception w){ System.exit(2);	}
	}
}
