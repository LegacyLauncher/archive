package com.turikhay.tlauncher.minecraft.profiles;

import java.io.File;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;

public class Profile {
   public static final ReleaseType[] DEFAULT_RELEASE_TYPES;
   private String name;
   private File gameDir;
   private String lastVersionId;
   private String javaDir;
   private String javaArgs;
   private Profile.Resolution resolution;
   private ReleaseType[] allowedReleaseTypes;
   private String playerUUID;
   private Boolean useHopperCrashService;
   private Profile.ActionOnClose launcherVisibilityOnGameClose;

   static {
      DEFAULT_RELEASE_TYPES = new ReleaseType[]{ReleaseType.RELEASE, ReleaseType.SNAPSHOT, ReleaseType.OLD};
   }

   public String toString() {
      return "Profile{name='" + this.name + "', gameDir='" + this.gameDir + "', lastVersion='" + this.lastVersionId + "', javaDir='" + this.javaDir + "', javaArgs='" + this.javaArgs + "', resolution='" + this.resolution + "', playerUUID=" + this.playerUUID + ", useHopper='" + this.useHopperCrashService + "', onClose='" + this.launcherVisibilityOnGameClose + "'}";
   }

   public Profile(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public File getGameDir() {
      return this.gameDir;
   }

   public void setGameDir(File gameDir) {
      this.gameDir = gameDir;
   }

   public void setLastVersionId(String lastVersionId) {
      this.lastVersionId = lastVersionId;
   }

   public void setJavaDir(String javaDir) {
      this.javaDir = javaDir;
   }

   public void setJavaArgs(String javaArgs) {
      this.javaArgs = javaArgs;
   }

   public String getLastVersionId() {
      return this.lastVersionId;
   }

   public String getJavaArgs() {
      return this.javaArgs;
   }

   public String getJavaPath() {
      return this.javaDir;
   }

   public Profile.Resolution getResolution() {
      return this.resolution;
   }

   public void setResolution(Profile.Resolution resolution) {
      this.resolution = resolution;
   }

   public String getPlayerUUID() {
      return this.playerUUID;
   }

   public void setPlayerUUID(String playerUUID) {
      this.playerUUID = playerUUID;
   }

   public ReleaseType[] getAllowedReleaseTypes() {
      return this.allowedReleaseTypes;
   }

   public void setAllowedReleaseTypes(ReleaseType[] allowedReleaseTypes) {
      this.allowedReleaseTypes = allowedReleaseTypes;
   }

   public boolean getUseHopperCrashService() {
      return this.useHopperCrashService == null;
   }

   public void setUseHopperCrashService(boolean useHopperCrashService) {
      this.useHopperCrashService = useHopperCrashService ? null : false;
   }

   public VersionFilter getVersionFilter() {
      VersionFilter filter = new VersionFilter();
      filter.onlyForTypes(this.allowedReleaseTypes == null ? DEFAULT_RELEASE_TYPES : this.allowedReleaseTypes);
      return filter;
   }

   public Profile.ActionOnClose getLauncherVisibilityOnGameClose() {
      return this.launcherVisibilityOnGameClose;
   }

   public void setLauncherVisibilityOnGameClose(Profile.ActionOnClose launcherVisibilityOnGameClose) {
      this.launcherVisibilityOnGameClose = launcherVisibilityOnGameClose;
   }

   public static enum ActionOnClose {
      HIDE_LAUNCHER("Hide launcher and re-open when game closes"),
      CLOSE_LAUNCHER("Close launcher when game starts"),
      DO_NOTHING("Keep the launcher open");

      private final String name;

      private ActionOnClose(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public String toString() {
         return this.name;
      }
   }

   public static class Resolution {
      private int width;
      private int height;

      public Resolution() {
      }

      public Resolution(Profile.Resolution resolution) {
         this(resolution.getWidth(), resolution.getHeight());
      }

      public Resolution(int width, int height) {
         this.width = width;
         this.height = height;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public String toString() {
         return this.width + "x" + this.height;
      }
   }
}
