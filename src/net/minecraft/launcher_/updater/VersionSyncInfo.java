package net.minecraft.launcher_.updater;

import net.minecraft.launcher_.versions.Version;
import net.minecraft.launcher_.versions.VersionSource;

public class VersionSyncInfo
{
  private final Version localVersion, remoteVersion, extraVersion;
  private final boolean isInstalled;
  private final boolean isUpToDate;
  private final VersionSource remoteSource, source;

  public VersionSyncInfo(Version localVersion, Version remoteVersion, Version extraVersion, boolean installed, boolean upToDate, VersionSource remoteSource, VersionSource source)
  {
    this.localVersion = localVersion;
    this.remoteVersion = remoteVersion;
    this.extraVersion = extraVersion;
    this.isInstalled = installed;
    this.isUpToDate = upToDate;
    
    this.remoteSource = remoteSource;
    this.source = source;
  }

  public Version getLocalVersion() {
    return this.localVersion;
  }

  public Version getRemoteVersion() {
    return this.remoteVersion;
  }
  
  public String getId(){
	  if(localVersion != null) return localVersion.getId();
	  if(extraVersion != null) return extraVersion.getId();
	  if(remoteVersion != null) return remoteVersion.getId();
	  
	  throw new IllegalStateException("Cannot get ID!");
  }

  public Version getLatestVersion() {
    switch(getLatestSource()){
	case EXTRA:
		return extraVersion;
	case REMOTE:
		return remoteVersion;
	case LOCAL:
		return localVersion;
    }
    throw new IllegalStateException("Unknown version source!");
  }

  public VersionSource getLatestSource()
  {
	  if(localVersion != null)
		  if(remoteVersion != null && remoteVersion.getUpdatedTime().after(localVersion.getUpdatedTime()))
			  return VersionSource.REMOTE;
		  else if(extraVersion != null && extraVersion.getUpdatedTime().after(localVersion.getUpdatedTime()))
			  return VersionSource.EXTRA;
	  
	  return source;
  }
  
  public VersionSource getRemoteSource(){
	  return remoteSource;
  }

  public boolean isInstalled() {
    return this.isInstalled;
  }

  public boolean isOnRemote() {
    return this.remoteVersion != null || this.extraVersion != null;
  }

  public boolean isUpToDate() {
    return this.isUpToDate;
  }

  public String toString()
  {
    return "VersionSyncInfo{localVersion=" + this.localVersion + ", remoteVersion=" + this.remoteVersion + ", isInstalled=" + this.isInstalled + ", isUpToDate=" + this.isUpToDate + '}';
  }
}