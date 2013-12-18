package net.minecraft.launcher.events;

import net.minecraft.launcher.updater.VersionManager;

public abstract interface RefreshedListener
{
  public abstract void onVersionManagerUpdated(VersionManager vm);
  public abstract void onVersionsRefreshing(VersionManager vm);
  public abstract void onVersionsRefreshingFailed(VersionManager vm);
  public abstract void onVersionsRefreshed(VersionManager vm);
  public abstract void onResourcesRefreshing(VersionManager vm);
  public abstract void onResourcesRefreshed(VersionManager vm);
}
