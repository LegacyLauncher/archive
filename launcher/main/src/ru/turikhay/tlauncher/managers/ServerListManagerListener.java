package ru.turikhay.tlauncher.managers;

public interface ServerListManagerListener {
    void onServersRefreshing(ServerListManager var1);

    void onServersRefreshingFailed(ServerListManager var1);

    void onServersRefreshed(ServerListManager var1);
}
