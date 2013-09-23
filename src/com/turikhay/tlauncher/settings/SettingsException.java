package com.turikhay.tlauncher.settings;

public class SettingsException extends RuntimeException {
	private static final long serialVersionUID = -3941711876079623953L;
	
	public SettingsException(Settings cl, String description, Throwable cause){
		super(description + " (" +cl.filename+")", cause);
	}
	
	public SettingsException(Settings cl, String description){
		super(description + " (" +cl.filename+")");
	}
	
	public SettingsException(String description){
		super(description);
	}
}
