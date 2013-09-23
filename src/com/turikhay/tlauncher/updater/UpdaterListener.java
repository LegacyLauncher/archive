package com.turikhay.tlauncher.updater;

public interface UpdaterListener {
	public void onUpdaterRequesting(Updater u);
	public void onUpdaterRequestError(Updater u, Throwable e);
	public void onUpdaterFoundUpdate(Updater u);
	public void onUpdaterNotFoundUpdate(Updater u);
	public void onUpdaterDownloading(Updater u);
	public void onUpdaterDownloadSuccess(Updater u);
	public void onUpdaterDownloadError(Updater u, Throwable e);
	public void onUpdaterProcessError(Updater u, Throwable e);
}
