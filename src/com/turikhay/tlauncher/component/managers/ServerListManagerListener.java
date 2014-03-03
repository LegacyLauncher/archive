package com.turikhay.tlauncher.component.managers;

public interface ServerListManagerListener {
   void onServersRefreshing(ServerListManager var1);

   void onServersRefreshingFailed(ServerListManager var1);

   void onServersRefreshed(ServerListManager var1);
}
