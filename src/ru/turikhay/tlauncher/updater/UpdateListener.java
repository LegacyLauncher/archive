package ru.turikhay.tlauncher.updater;

public interface UpdateListener {
	public void onUpdateError(Update u, Throwable e);

	public void onUpdateDownloading(Update u);

	public void onUpdateDownloadError(Update u, Throwable e);

	public void onUpdateReady(Update u);

	public void onUpdateApplying(Update u);

	public void onUpdateApplyError(Update u, Throwable e);
}
