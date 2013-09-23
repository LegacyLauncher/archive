package net.minecraft.launcher_.events;

import net.minecraft.launcher_.updater.VersionManager;

public abstract interface RefreshedListener
{
  public abstract void onVersionsRefreshing(VersionManager vm);
  public abstract void onVersionsRefreshingFailed(VersionManager vm);
  public abstract void onVersionsRefreshed(VersionManager vm);
  public abstract void onResourcesRefreshing(VersionManager vm);
  public abstract void onResourcesRefreshed(VersionManager vm);
}
