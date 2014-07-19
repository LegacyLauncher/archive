package ru.turikhay.tlauncher.downloader;

public interface DownloaderListener {
	public void onDownloaderStart(Downloader d, int files);

	public void onDownloaderAbort(Downloader d);

	public void onDownloaderProgress(Downloader d, double progress, double speed);

	public void onDownloaderFileComplete(Downloader d, Downloadable file);

	public void onDownloaderComplete(Downloader d);
}
