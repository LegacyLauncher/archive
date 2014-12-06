package com.turikhay.tlauncher.downloader;

public interface DownloadableHandler {
	void onStart(Downloadable d);

	void onAbort(Downloadable d);

	void onComplete(Downloadable d) throws RetryDownloadException;

	void onError(Downloadable d, Throwable e);
}
