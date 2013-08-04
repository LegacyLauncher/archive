package net.minecraft.launcher_.events;

import net.minecraft.launcher_.updater.VersionManager;

public interface RefreshedVersionsListener {
   void onVersionsRefreshing(VersionManager var1);

   void onVersionsRefreshingFailed(VersionManager var1);

   void onVersionsRefreshed(VersionManager var1);
}
