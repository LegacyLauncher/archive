package com.turikhay.tlauncher.component.managers;

public interface ServerListManagerListener {
	public void onServersRefreshing(ServerListManager sm);
	public void onServersRefreshingFailed(ServerListManager sm);
	public void onServersRefreshed(ServerListManager sm);

}
