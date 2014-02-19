package com.turikhay.tlauncher.updater;

public interface UpdaterListener {
	public void onUpdaterRequesting(Updater u);
	public void onUpdaterRequestError(Updater u);
	public void onUpdateFound(Update upd);
	public void onUpdaterNotFoundUpdate(Updater u);
	
	public void onAdFound(Updater u, Ad ad);
}
