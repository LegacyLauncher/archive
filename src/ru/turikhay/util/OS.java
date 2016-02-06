package ru.turikhay.util;

import com.sun.management.OperatingSystemMXBean;
import java.awt.Desktop;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.turikhay.tlauncher.ui.alert.Alert;

public enum OS {
   LINUX(new String[]{"linux", "unix"}),
   WINDOWS(new String[]{"win"}),
   OSX(new String[]{"mac"}),
   SOLARIS(new String[]{"solaris", "sunos"}),
   UNKNOWN(new String[]{"unknown"});

   public static final String NAME = System.getProperty("os.name");
   public static final String VERSION = System.getProperty("os.version");
   public static final double JAVA_VERSION = getJavaVersion();
   public static final OS CURRENT = getCurrent();
   private final String name;
   private final String[] aliases;

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

   public boolean isCurrent() {
      return this == CURRENT;
   }

   private static OS getCurrent() {
      String osName = NAME.toLowerCase();
      OS[] var4;
      int var3 = (var4 = values()).length;

      for(int var2 = 0; var2 < var3; ++var2) {
         OS os = var4[var2];
         String[] var8 = os.aliases;
         int var7 = os.aliases.length;

         for(int var6 = 0; var6 < var7; ++var6) {
            String alias = var8[var6];
            if (osName.contains(alias)) {
               return os;
            }
         }
      }

      return UNKNOWN;
   }

   private static double getJavaVersion() {
      Pattern syntaxNotationPattern = Pattern.compile(".*(\\-.+)");
      Pattern versionPattern = Pattern.compile("([\\d]+(?:\\.\\d+)?)(?:\\.\\d+)?(?:_\\d+)?");
      String version = System.getProperty("java.version");
      String _version = version;
      Matcher syntaxNotationMatcher = syntaxNotationPattern.matcher(version);
      if (syntaxNotationMatcher.matches()) {
         version = syntaxNotationMatcher.replaceAll("");
      }

      Matcher versionMatcher = versionPattern.matcher(version);
      if (versionMatcher.matches()) {
         return Double.parseDouble(versionMatcher.group(1));
      } else {
         U.log("[ERROR] Could not determine Java version:", _version);
         U.log("I suppose we use at least 1.6");
         return 1.6D;
      }
   }

   public static boolean is(OS... any) {
      if (any == null) {
         throw new NullPointerException();
      } else if (any.length == 0) {
         return false;
      } else {
         OS[] var4 = any;
         int var3 = any.length;

         for(int var2 = 0; var2 < var3; ++var2) {
            OS compare = var4[var2];
            if (CURRENT == compare) {
               return true;
            }
         }

         return false;
      }
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
      return NAME + " (" + VERSION + ") " + OS.Arch.CURRENT + ", Java " + System.getProperty("java.version") + " (" + JAVA_VERSION + "), " + OS.Arch.TOTAL_RAM_MB + " MB RAM, " + OS.Arch.AVAILABLE_PROCESSORS + "x CPU";
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

   public static boolean openLink(URL url, boolean alertError) {
      log("Trying to open URL with default browser:", url);
      URI uri = null;

      try {
         uri = url.toURI();
      } catch (Exception var4) {
      }

      return openLink(uri, alertError);
   }

   public static boolean openLink(URL url) {
      return openLink(url, true);
   }

   private static void openPath(File path, boolean appendSeparator) throws Throwable {
      String absPath = path.getAbsolutePath() + File.separatorChar;
      Runtime r = Runtime.getRuntime();
      Throwable t = null;
      switch(CURRENT) {
      case WINDOWS:
         String cmd = String.format("cmd.exe /C start \"Open path\" \"%s\"", absPath);

         try {
            r.exec(cmd);
            return;
         } catch (Throwable var10) {
            t = var10;
            log("Cannot open folder using CMD.exe:\n", cmd, var10);
            break;
         }
      case OSX:
         String[] e = new String[]{"/usr/bin/open", absPath};

         try {
            r.exec(e);
            return;
         } catch (Throwable var9) {
            t = var9;
            log("Cannot open folder using:\n", e, var9);
            break;
         }
      default:
         log("... will use desktop");
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
            log("Failed to open folder:", var3);
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
            log("Failed to open file:", var3);
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

   public static enum Arch {
      x32,
      x64,
      UNKNOWN;

      public static final OS.Arch CURRENT = getCurrent();
      public static final long TOTAL_RAM = getTotalRam();
      public static final long TOTAL_RAM_MB = TOTAL_RAM / 1024L / 1024L;
      public static final int PREFERRED_MEMORY = getPreferredMemory();
      public static final int MAX_MEMORY = getMaximumMemory();
      public static final int AVAILABLE_PROCESSORS = getAvailableProcessors();
      private final String asString = this.toString().substring(1);
      private final int asInt;

      private Arch() {
         int asInt_temp = 0;

         try {
            asInt_temp = Integer.parseInt(this.asString);
         } catch (RuntimeException var5) {
         }

         this.asInt = asInt_temp;
      }

      public String asString() {
         return this == UNKNOWN ? this.toString() : this.asString;
      }

      public boolean isCurrent() {
         return this == CURRENT;
      }

      private static OS.Arch getCurrent() {
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

      private static long getTotalRam() {
         try {
            return ((OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
         } catch (Throwable var1) {
            U.log("[ERROR] Cannot allocate total physical memory size!", var1);
            return 0L;
         }
      }

      private static int getAvailableProcessors() {
         try {
            return ((OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getAvailableProcessors();
         } catch (Throwable var1) {
            U.log("[ERROR] Cannot determine available processors", var1);
            return 1;
         }
      }

      private static int getPreferredMemory() {
         switch(CURRENT) {
         case x64:
            return 1024;
         case x32:
            if (TOTAL_RAM_MB > 4000L) {
               return 768;
            }
         default:
            return 512;
         }
      }

      private static int getMaximumMemory() {
         switch(CURRENT) {
         case x64:
            if (TOTAL_RAM_MB > 6000L) {
               return 3072;
            } else {
               if (TOTAL_RAM_MB > 3000L) {
                  return 1536;
               }

               return 1024;
            }
         case x32:
            if (TOTAL_RAM_MB > 4000L) {
               return 1536;
            }

            return 1024;
         default:
            return 512;
         }
      }
   }
}
