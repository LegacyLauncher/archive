package ru.turikhay.tlauncher.downloader;

public interface DownloadableContainerHandler {
	void onStart(DownloadableContainer c);

	void onAbort(DownloadableContainer c);

	void onError(DownloadableContainer c, Downloadable d, Throwable e);

	void onComplete(DownloadableContainer c, Downloadable d)
			throws RetryDownloadException;

	void onFullComplete(DownloadableContainer c);
}
