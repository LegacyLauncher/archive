package net.minecraft.launcher_.updater;

import net.minecraft.launcher_.versions.Version;

public class VersionSyncInfo {
   private final Version localVersion;
   private final Version remoteVersion;
   private final boolean isInstalled;
   private final boolean isUpToDate;

   public VersionSyncInfo(Version localVersion, Version remoteVersion, boolean installed, boolean upToDate) {
      this.localVersion = localVersion;
      this.remoteVersion = remoteVersion;
      this.isInstalled = installed;
      this.isUpToDate = upToDate;
   }

   public Version getLocalVersion() {
      return this.localVersion;
   }

   public Version getRemoteVersion() {
      return this.remoteVersion;
   }

   public Version getLatestVersion() {
      return this.getLatestSource() == VersionSyncInfo.VersionSource.REMOTE ? this.remoteVersion : this.localVersion;
   }

   public VersionSyncInfo.VersionSource getLatestSource() {
      if (this.getLocalVersion() == null) {
         return VersionSyncInfo.VersionSource.REMOTE;
      } else if (this.getRemoteVersion() == null) {
         return VersionSyncInfo.VersionSource.LOCAL;
      } else {
         return this.getRemoteVersion().getUpdatedTime().after(this.getLocalVersion().getUpdatedTime()) ? VersionSyncInfo.VersionSource.REMOTE : VersionSyncInfo.VersionSource.LOCAL;
      }
   }

   public boolean isInstalled() {
      return this.isInstalled;
   }

   public boolean isOnRemote() {
      return this.remoteVersion != null;
   }

   public boolean isUpToDate() {
      return this.isUpToDate;
   }

   public String toString() {
      return "VersionSyncInfo{localVersion=" + this.localVersion + ", remoteVersion=" + this.remoteVersion + ", isInstalled=" + this.isInstalled + ", isUpToDate=" + this.isUpToDate + '}';
   }

   public static enum VersionSource {
      REMOTE,
      LOCAL;
   }
}
