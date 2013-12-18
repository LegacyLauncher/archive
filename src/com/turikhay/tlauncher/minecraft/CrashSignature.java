package com.turikhay.tlauncher.minecraft;

import java.util.regex.Pattern;

public class CrashSignature {
	public final Pattern pattern;
	public final int exitcode;
	public final String name, path;
	
	CrashSignature(int exitcode, String pattern, String name, String path){
		this.pattern = (pattern != null)? Pattern.compile(pattern) : null;
		this.exitcode = exitcode;
		this.name = name;
		this.path = path;
	}
	
	public boolean match(String line){
		if(pattern == null) return false;
		return pattern.matcher(line).matches();
	}
	
	public boolean match(int exit){
		if(exitcode == 0) return false;
		return exit == exitcode;
	}
}
