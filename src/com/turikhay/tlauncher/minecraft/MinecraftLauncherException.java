package com.turikhay.tlauncher.minecraft;

import com.turikhay.util.U;

public class MinecraftLauncherException extends Exception
{
	private static final long serialVersionUID = 7704448719401222348L;
	private String langpath = "launcher.error.";
	private Object replace = null;

	public MinecraftLauncherException(String message) {
		super(message);

		this.langpath = null;
	}

	public MinecraftLauncherException(String message, String langpath) {
		super(message);

		this.langpath += langpath;
	}

	public MinecraftLauncherException(String message, String langpath, Object replace) {
		super(message);

		this.langpath += langpath;
		this.replace = ((replace instanceof Throwable) ? U.stackTrace((Throwable)replace) : replace);
	}
	
	public String getLangpath() { return this.langpath; } 
	public Object getReplace() { return this.replace; }
}
