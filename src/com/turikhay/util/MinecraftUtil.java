package com.turikhay.util;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import java.io.File;
import java.net.URL;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;

public class MinecraftUtil {
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher$OperatingSystem;

   public static File getWorkingDirectory() {
      if (TLauncher.getInstance() == null) {
         return getDefaultWorkingDirectory();
      } else {
         String dir = TLauncher.getInstance().getSettings().get("minecraft.gamedir");
         return dir == null ? getDefaultWorkingDirectory() : new File(dir);
      }
   }

   public static File getSystemRelatedFile(String path) {
      String userHome = System.getProperty("user.home", ".");
      File file;
      switch($SWITCH_TABLE$net$minecraft$launcher$OperatingSystem()[OperatingSystem.getCurrentPlatform().ordinal()]) {
      case 1:
      case 4:
         file = new File(userHome, path);
         break;
      case 2:
         String applicationData = System.getenv("APPDATA");
         String folder = applicationData != null ? applicationData : userHome;
         file = new File(folder, path);
         break;
      case 3:
         file = new File(userHome, "Library/Application Support/" + path);
         break;
      default:
         file = new File(userHome, path);
      }

      return file;
   }

   public static File getDefaultWorkingDirectory() {
      OperatingSystem os = OperatingSystem.getCurrentPlatform();
      String separator = File.separator;
      String path = ".minecraft";
      if (os == OperatingSystem.OSX || os == OperatingSystem.UNKNOWN) {
         path = "minecraft";
      }

      return getSystemRelatedFile(path + separator);
   }

   public static File getOptionsFile() {
      return getFile("options.txt");
   }

   public static File getFile(String name) {
      return new File(getWorkingDirectory(), name);
   }

   public static Downloadable getDownloadable(String url, boolean force) {
      URL url_r = null;

      try {
         url_r = new URL(url);
         return new Downloadable(url, getFile(FileUtil.getFilename(url_r)), force);
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public static Downloadable getDownloadable(String url) {
      return getDownloadable(url, false);
   }

   public static VersionFilter getVersionFilter() {
      TLauncher t = TLauncher.getInstance();
      if (t == null) {
         throw new IllegalStateException("TLauncher instance is not defined!");
      } else {
         VersionFilter r = new VersionFilter();
         boolean snaps = t.getSettings().getBoolean("minecraft.versions.snapshots");
         boolean beta = t.getSettings().getBoolean("minecraft.versions.beta");
         boolean alpha = t.getSettings().getBoolean("minecraft.versions.alpha");
         boolean cheats = t.getSettings().getBoolean("minecraft.versions.cheats");
         if (!snaps) {
            r.excludeType(ReleaseType.SNAPSHOT);
         }

         if (!beta) {
            r.excludeType(ReleaseType.OLD_BETA);
         }

         if (!alpha) {
            r.excludeType(ReleaseType.OLD_ALPHA);
         }

         if (!cheats) {
            r.excludeType(ReleaseType.CHEAT);
         }

         return r;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher$OperatingSystem() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher$OperatingSystem;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[OperatingSystem.values().length];

         try {
            var0[OperatingSystem.LINUX.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[OperatingSystem.OSX.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[OperatingSystem.SOLARIS.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[OperatingSystem.UNKNOWN.ordinal()] = 5;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[OperatingSystem.WINDOWS.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$net$minecraft$launcher$OperatingSystem = var0;
         return var0;
      }
   }
}