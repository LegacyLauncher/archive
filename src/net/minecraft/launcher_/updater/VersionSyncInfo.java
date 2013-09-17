package net.minecraft.launcher_.updater;

import net.minecraft.launcher_.versions.Version;
import net.minecraft.launcher_.versions.VersionSource;

public class VersionSyncInfo {
   private final Version localVersion;
   private final Version remoteVersion;
   private final Version extraVersion;
   private final boolean isInstalled;
   private final boolean isUpToDate;
   private final VersionSource remoteSource;
   private final VersionSource source;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource;

   public VersionSyncInfo(Version localVersion, Version remoteVersion, Version extraVersion, boolean installed, boolean upToDate, VersionSource remoteSource, VersionSource source) {
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

   public String getId() {
      if (this.localVersion != null) {
         return this.localVersion.getId();
      } else if (this.extraVersion != null) {
         return this.extraVersion.getId();
      } else if (this.remoteVersion != null) {
         return this.remoteVersion.getId();
      } else {
         throw new IllegalStateException("Cannot get ID!");
      }
   }

   public Version getLatestVersion() {
      switch($SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource()[this.getLatestSource().ordinal()]) {
      case 1:
         return this.localVersion;
      case 2:
         return this.remoteVersion;
      case 3:
         return this.extraVersion;
      default:
         throw new IllegalStateException("Unknown version source!");
      }
   }

   public VersionSource getLatestSource() {
      if (this.localVersion != null) {
         if (this.remoteVersion != null && this.remoteVersion.getUpdatedTime().after(this.localVersion.getUpdatedTime())) {
            return VersionSource.REMOTE;
         }

         if (this.extraVersion != null && this.extraVersion.getUpdatedTime().after(this.localVersion.getUpdatedTime())) {
            return VersionSource.EXTRA;
         }
      }

      return this.source;
   }

   public VersionSource getRemoteSource() {
      return this.remoteSource;
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

   public String toString() {
      return "VersionSyncInfo{localVersion=" + this.localVersion + ", remoteVersion=" + this.remoteVersion + ", isInstalled=" + this.isInstalled + ", isUpToDate=" + this.isUpToDate + '}';
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[VersionSource.values().length];

         try {
            var0[VersionSource.EXTRA.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[VersionSource.LOCAL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[VersionSource.REMOTE.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource = var0;
         return var0;
      }
   }
}
