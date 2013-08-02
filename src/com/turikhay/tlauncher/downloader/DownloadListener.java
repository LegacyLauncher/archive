package com.turikhay.tlauncher.downloader;

public interface DownloadListener {
	public void onDownloaderStart(Downloader d, int files);
	public void onDownloaderError(Downloader d, Downloadable file, Throwable error);
	public void onDownloaderProgress(Downloader d, int progress);
	public void onDownloaderFileComplete(Downloader d, Downloadable file);
	public void onDownloaderComplete(Downloader d);
}
