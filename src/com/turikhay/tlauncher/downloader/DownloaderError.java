package com.turikhay.tlauncher.downloader;

public class DownloaderError extends RuntimeException {
	private static final long serialVersionUID = 3468802480206284946L;
	private boolean serious;
	private int timeout;
	
	public DownloaderError(String message, boolean isSerious){
		super(message);
		
		serious = isSerious;
	}
	public DownloaderError(String message, int recommended_timeout){
		super(message);
		
		serious = false;
		timeout = recommended_timeout;
	}
	public boolean isSerious(){ return serious; }
	public int getTimeout(){ return timeout; }
	public boolean hasTimeout(){ return (timeout > 0); }

}
