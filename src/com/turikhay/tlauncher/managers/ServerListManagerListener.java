package com.turikhay.tlauncher.managers;

public interface ServerListManagerListener {
	public void onServersRefreshing(ServerListManager sm);

	public void onServersRefreshingFailed(ServerListManager sm);

	public void onServersRefreshed(ServerListManager sm);

}
