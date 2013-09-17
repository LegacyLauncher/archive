package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.downloader.Downloadable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.updater.VersionFilter;
import net.minecraft.launcher_.versions.ReleaseType;

public class MinecraftUtil {
   private static TLauncher t;
   private static File g_workingDirectory;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher_$OperatingSystem;

   static void setWorkingTo(TLauncher to) {
      if (t == null) {
         t = to;
      }

   }

   public static void setCustomWorkingDirectory(File folder) {
      if (!folder.isDirectory()) {
         folder.mkdirs();
      }

      g_workingDirectory = folder;
   }

   public static File getWorkingDirectory() {
      if (g_workingDirectory != null) {
         return g_workingDirectory;
      } else {
         String userHome = System.getProperty("user.home", ".");
         String separator = File.separator;
         File workingDirectory;
         switch($SWITCH_TABLE$net$minecraft$launcher_$OperatingSystem()[OperatingSystem.getCurrentPlatform().ordinal()]) {
         case 1:
         case 4:
            workingDirectory = new File(userHome, ".minecraft" + separator);
            break;
         case 2:
            String applicationData = System.getenv("APPDATA");
            String folder = applicationData != null ? applicationData : userHome;
            workingDirectory = new File(folder, ".minecraft" + separator);
            break;
         case 3:
            workingDirectory = new File(userHome, "Library" + separator + "Application Support" + separator + "minecraft");
            break;
         default:
            workingDirectory = new File(userHome, "minecraft" + separator);
         }

         g_workingDirectory = workingDirectory;
         return workingDirectory;
      }
   }

   public static File getOptionsFile() {
      return getFile("options.txt");
   }

   public static File getNativeOptionsFile() {
      return getFile("toptions.ini");
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

   public static void startLauncher(File launcherJar, Class[] construct, Object[] obj) {
      U.log("Starting launcher...");

      try {
         Class aClass = (new URLClassLoader(new URL[]{launcherJar.toURI().toURL()})).loadClass("net.minecraft.launcher.Launcher");
         Constructor constructor = aClass.getConstructor(construct);
         constructor.newInstance(obj);
      } catch (Exception var5) {
         throw new TLauncherException("Cannot start launcher", var5);
      }
   }

   public static VersionFilter getVersionFilter() {
      VersionFilter r = new VersionFilter();
      boolean snaps = t.settings.getBoolean("minecraft.versions.snapshots");
      boolean beta = t.settings.getBoolean("minecraft.versions.beta");
      boolean alpha = t.settings.getBoolean("minecraft.versions.alpha");
      if (!snaps) {
         r.excludeType(ReleaseType.SNAPSHOT);
      }

      if (!beta) {
         r.excludeType(ReleaseType.OLD_BETA);
      }

      if (!alpha) {
         r.excludeType(ReleaseType.OLD_ALPHA);
      }

      return r;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher_$OperatingSystem() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher_$OperatingSystem;
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

         $SWITCH_TABLE$net$minecraft$launcher_$OperatingSystem = var0;
         return var0;
      }
   }
}
