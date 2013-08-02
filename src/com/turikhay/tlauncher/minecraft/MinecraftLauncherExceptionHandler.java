package com.turikhay.tlauncher.minecraft;

public class MinecraftLauncherExceptionHandler implements Thread.UncaughtExceptionHandler {
	private MinecraftLauncher l;
	
	MinecraftLauncherExceptionHandler(MinecraftLauncher l){
		this.l = l;
	}

	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		l.onError(e);
	}

}
