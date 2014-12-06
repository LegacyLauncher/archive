package ru.turikhay.tlauncher.downloader;

import java.io.IOException;

/**
 * Exception that teels DownloaderThread to retry to download a file.
 */
public class RetryDownloadException extends IOException {
	private static final long serialVersionUID = 2968569164701826930L;

	public RetryDownloadException(String message) {
		super(message);
	}

	public RetryDownloadException(String message, Throwable cause) {
		super(message, cause);
	}

}
