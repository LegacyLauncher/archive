package com.turikhay.tlauncher.downloader;

public interface DownloadListener {
	public void onDownloaderStart(Downloader d, int files);
	public void onDownloaderAbort(Downloader d);
	public void onDownloaderError(Downloader d, Downloadable file, Throwable error);
	public void onDownloaderProgress(Downloader d, int progress, double speed);
	public void onDownloaderFileComplete(Downloader d, Downloadable file);
	public void onDownloaderComplete(Downloader d);
}
