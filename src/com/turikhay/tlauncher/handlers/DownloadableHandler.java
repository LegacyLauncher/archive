package com.turikhay.tlauncher.handlers;

public interface DownloadableHandler {
	public void onStart();
	public void onCompleteError();
	public void onComplete();
}
