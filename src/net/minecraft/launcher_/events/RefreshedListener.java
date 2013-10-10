package net.minecraft.launcher_.events;

import net.minecraft.launcher_.updater.VersionManager;

public interface RefreshedListener {
   void onVersionManagerUpdated(VersionManager var1);

   void onVersionsRefreshing(VersionManager var1);

   void onVersionsRefreshingFailed(VersionManager var1);

   void onVersionsRefreshed(VersionManager var1);

   void onResourcesRefreshing(VersionManager var1);

   void onResourcesRefreshed(VersionManager var1);
}
