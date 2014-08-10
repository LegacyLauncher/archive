package ru.turikhay.util;

import java.io.File;
import java.io.IOException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;

public class MinecraftUtil {
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$util$OS;

   public static File getWorkingDirectory() {
      if (TLauncher.getInstance() == null) {
         return getDefaultWorkingDirectory();
      } else {
         Configuration settings = TLauncher.getInstance().getSettings();
         String sdir = settings.get("minecraft.gamedir");
         if (sdir == null) {
            return getDefaultWorkingDirectory();
         } else {
            File dir = new File(sdir);

            try {
               FileUtil.createFolder(dir);
               return dir;
            } catch (IOException var4) {
               U.log("Cannot create specified Minecraft folder:", dir.getAbsolutePath());
               return getDefaultWorkingDirectory();
            }
         }
      }
   }

   public static File getSystemRelatedFile(String path) {
      String userHome = System.getProperty("user.home", ".");
      File file;
      switch($SWITCH_TABLE$ru$turikhay$util$OS()[OS.CURRENT.ordinal()]) {
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

   public static File getSystemRelatedDirectory(String path) {
      if (!OS.is(OS.OSX, OS.UNKNOWN)) {
         path = '.' + path;
      }

      return getSystemRelatedFile(path);
   }

   public static File getDefaultWorkingDirectory() {
      return getSystemRelatedDirectory(TLauncher.getFolder());
   }

   public static File getOptionsFile() {
      return getFile("options.txt");
   }

   private static File getFile(String name) {
      return new File(getWorkingDirectory(), name);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$util$OS() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$util$OS;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[OS.values().length];

         try {
            var0[OS.LINUX.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[OS.OSX.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[OS.SOLARIS.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[OS.UNKNOWN.ordinal()] = 5;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[OS.WINDOWS.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$util$OS = var0;
         return var0;
      }
   }
}
