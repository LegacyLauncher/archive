package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class LatestVersionSyncInfo extends VersionSyncInfo {
   private final ReleaseType type;

   public LatestVersionSyncInfo(ReleaseType type, Version localVersion, Version remoteVersion) {
      super(localVersion, remoteVersion);
      if (type == null) {
         throw new NullPointerException("ReleaseType cannot be NULL!");
      } else {
         this.type = type;
         this.setID("latest-" + type.toString());
      }
   }

   public LatestVersionSyncInfo(ReleaseType type, VersionSyncInfo syncInfo) {
      this(type, syncInfo.getLocal(), syncInfo.getRemote());
   }

   public String getVersionID() {
      if (this.localVersion != null) {
         return this.localVersion.getID();
      } else {
         return this.remoteVersion != null ? this.remoteVersion.getID() : null;
      }
   }

   public ReleaseType getReleaseType() {
      return this.type;
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{id='" + this.getID() + "', releaseType=" + this.type + ",\nlocal=" + this.localVersion + ",\nremote=" + this.remoteVersion + ", isInstalled=" + this.isInstalled() + ", hasRemote=" + this.hasRemote() + ", isUpToDate=" + this.isUpToDate() + "}";
   }
}
