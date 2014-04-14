package com.turikhay.util;

import com.turikhay.tlauncher.ui.alert.Alert;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public enum OS {
   LINUX(new String[]{"linux", "unix"}),
   WINDOWS(new String[]{"win"}),
   OSX(new String[]{"mac"}),
   SOLARIS(new String[]{"solaris", "sunos"}),
   UNKNOWN(new String[]{"unknown"});

   private final String name;
   private final String[] aliases;
   public static final OS CURRENT = getCurrent();
   public static final OS.Arch ARCH = OS.Arch.getCurrent();
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$util$OS;

   private OS(String... aliases) {
      if (aliases == null) {
         throw new NullPointerException();
      } else {
         this.name = this.toString().toLowerCase();
         this.aliases = aliases;
      }
   }

   public String getName() {
      return this.name;
   }

   public boolean isUnsupported() {
      return this == UNKNOWN;
   }

   public boolean isCurrent() {
      return this == CURRENT;
   }

   public static OS getCurrent() {
      String osName = System.getProperty("os.name").toLowerCase();
      OS[] var4;
      int var3 = (var4 = values()).length;

      for(int var2 = 0; var2 < var3; ++var2) {
         OS os = var4[var2];
         String[] var8;
         int var7 = (var8 = os.aliases).length;

         for(int var6 = 0; var6 < var7; ++var6) {
            String alias = var8[var6];
            if (osName.contains(alias)) {
               return os;
            }
         }
      }

      return UNKNOWN;
   }

   public static OS.Arch getArch() {
      return OS.Arch.getCurrent();
   }

   public static String getJavaPath(boolean appendBinFolder) {
      char separator = File.separatorChar;
      String path = System.getProperty("java.home") + separator;
      if (appendBinFolder) {
         path = path + "bin" + separator + "java";
         if (CURRENT == WINDOWS) {
            path = path + "w.exe";
         }
      }

      return path;
   }

   public static String getJavaPath() {
      return getJavaPath(true);
   }

   public static String getSummary() {
      return System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + "Java " + System.getProperty("java.version");
   }

   private static void rawOpenLink(URI uri) throws Throwable {
      Desktop.getDesktop().browse(uri);
   }

   public static boolean openLink(URI uri, boolean alertError) {
      log("Trying to open link with default browser:", uri);

      try {
         Desktop.getDesktop().browse(uri);
         return true;
      } catch (Throwable var3) {
         log("Failed to open link with default browser:", uri, var3);
         if (alertError) {
            Alert.showLocError("ui.error.openlink", uri);
         }

         return false;
      }
   }

   public static boolean openLink(URI uri) {
      return openLink(uri, true);
   }

   private static void openPath(File path, boolean appendSeparator) throws Throwable {
      String absPath = path.getAbsolutePath() + File.separatorChar;
      Runtime r = Runtime.getRuntime();
      Throwable t = null;
      switch($SWITCH_TABLE$com$turikhay$util$OS()[CURRENT.ordinal()]) {
      case 1:
      case 3:
         String[] cmdArr = new String[]{"/usr/bin/open", absPath};

         try {
            r.exec(cmdArr);
            return;
         } catch (Throwable var10) {
            t = var10;
            log("Cannot open folder using:", cmdArr, var10);
            break;
         }
      case 2:
         String cmd = String.format("cmd.exe /C start \"Open file\" \"%s\"", StringUtil.addSlashes(absPath, StringUtil.EscapeGroup.DOUBLE_QUOTE));

         try {
            r.exec(cmd);
            return;
         } catch (Throwable var9) {
            t = var9;
            log("Cannot open folder using CMD.exe\n", cmd, var9);
            break;
         }
      default:
         log("Unknown system: will use desktop");
      }

      try {
         rawOpenLink(path.toURI());
      } catch (Throwable var8) {
         t = var8;
      }

      if (t != null) {
         throw t;
      }
   }

   public static boolean openFolder(File folder, boolean alertError) {
      log("Trying to open folder:", folder);
      if (!folder.isDirectory()) {
         log("This path is not a directory, sorry.");
         return false;
      } else {
         try {
            openPath(folder, true);
            return true;
         } catch (Throwable var3) {
            log("Failed to open folder:", folder, var3);
            if (alertError) {
               Alert.showLocError("ui.error.openfolder", folder);
            }

            return false;
         }
      }
   }

   public static boolean openFolder(File folder) {
      return openFolder(folder, true);
   }

   public static boolean openFile(File file, boolean alertError) {
      log("Trying to open file:", file);
      if (!file.isFile()) {
         log("This path is not a file, sorry.");
         return false;
      } else {
         try {
            openPath(file, false);
            return true;
         } catch (Throwable var3) {
            log("Failed to open file:", file, var3);
            if (alertError) {
               Alert.showLocError("ui.error.openfolder", file);
            }

            return false;
         }
      }
   }

   public static boolean openFile(File file) {
      return openFile(file, true);
   }

   protected static void log(Object... o) {
      U.log("[OS]", o);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$util$OS() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$util$OS;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[values().length];

         try {
            var0[LINUX.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[OSX.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[SOLARIS.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[UNKNOWN.ordinal()] = 5;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[WINDOWS.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$util$OS = var0;
         return var0;
      }
   }

   public static enum Arch {
      x32,
      x64,
      UNKNOWN;

      public static final OS.Arch CURRENT = getCurrent();
      private final String asString = this.toString().substring(1);
      private final byte asByte;

      private Arch() {
         byte asByte_temp = 0;

         try {
            asByte_temp = Byte.parseByte(this.asString);
         } catch (RuntimeException var5) {
         }

         this.asByte = asByte_temp;
      }

      public String asString() {
         return this.asString;
      }

      public byte asByte() {
         return this.asByte;
      }

      public boolean isCurrent() {
         return this == CURRENT;
      }

      public static OS.Arch getCurrent() {
         String curArch = System.getProperty("sun.arch.data.model");
         OS.Arch[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            OS.Arch arch = var4[var2];
            if (arch.asString.equals(curArch)) {
               return arch;
            }
         }

         return UNKNOWN;
      }

      public static int getRecommendedMemory() {
         return CURRENT != x32 && CURRENT != UNKNOWN ? 1024 : 512;
      }
   }
}
