package com.turikhay.tlauncher.downloader;

public interface DownloadableHandler {
	public void onStart();
	public void onCompleteError();
	public void onComplete();
}
