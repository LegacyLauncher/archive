package ru.turikhay.tlauncher.managers;

public interface VersionManagerListener {
	public void onVersionsRefreshing(VersionManager manager);

	public void onVersionsRefreshingFailed(VersionManager manager);

	public void onVersionsRefreshed(VersionManager manager);
}
