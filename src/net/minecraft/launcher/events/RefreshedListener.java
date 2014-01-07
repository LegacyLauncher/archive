package net.minecraft.launcher.events;

import net.minecraft.launcher.updater.VersionManager;

public interface RefreshedListener {
   void onVersionManagerUpdated(VersionManager var1);

   void onVersionsRefreshing(VersionManager var1);

   void onVersionsRefreshingFailed(VersionManager var1);

   void onVersionsRefreshed(VersionManager var1);
}
