package net.minecraft.launcher;

import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public enum OperatingSystem {
   LINUX("linux", new String[]{"linux", "unix"}),
   WINDOWS("windows", new String[]{"win"}),
   OSX("osx", new String[]{"mac"}),
   SOLARIS("solaris", new String[]{"solaris", "sunos"}),
   UNKNOWN("unknown", new String[0]);

   private final String name;
   private final String[] aliases;
   private final String arch = System.getProperty("sun.arch.data.model");

   private OperatingSystem(String name, String[] aliases) {
      this.name = name;
      this.aliases = aliases == null ? new String[0] : aliases;
   }

   public String getName() {
      return this.name;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   public boolean isSupported() {
      return this != UNKNOWN;
   }

   public String getJavaDir(boolean appendFile) {
      String separator = System.getProperty("file.separator");
      String path = System.getProperty("java.home") + separator;
      if (appendFile) {
         path = path + "bin" + separator;
         return getCurrentPlatform() == WINDOWS ? path + "javaw.exe" : path + "java";
      } else {
         return path;
      }
   }

   public String getJavaDir() {
      return this.getJavaDir(true);
   }

   public boolean doesJavaExist() {
      return this == WINDOWS ? FileUtil.fileExists(this.getJavaDir()) : FileUtil.folderExists(this.getJavaDir());
   }

   public static OperatingSystem getCurrentPlatform() {
      String osName = System.getProperty("os.name").toLowerCase();
      OperatingSystem[] var4;
      int var3 = (var4 = values()).length;

      for(int var2 = 0; var2 < var3; ++var2) {
         OperatingSystem os = var4[var2];
         String[] var8;
         int var7 = (var8 = os.getAliases()).length;

         for(int var6 = 0; var6 < var7; ++var6) {
            String alias = var8[var6];
            if (osName.contains(alias)) {
               return os;
            }
         }
      }

      return UNKNOWN;
   }

   public boolean is32Bit() {
      return this.arch.equals("32");
   }

   public boolean is64Bit() {
      return this.arch.equals("64");
   }

   public String getArch() {
      return this.arch;
   }

   private static void rawOpenLink(URI uri) throws Throwable {
      Desktop desktop = Desktop.getDesktop();
      desktop.browse(uri);
   }

   public static boolean openLink(URI uri, boolean showError) {
      try {
         rawOpenLink(uri);
         return true;
      } catch (Throwable var3) {
         U.log("Cannot browser link:", uri);
         if (showError) {
            Alert.showError(Localizable.get("ui.error.openlink.title"), Localizable.get("ui.error.openlink", uri), var3);
         }

         return false;
      }
   }

   public static boolean openLink(URI uri) {
      return openLink(uri, true);
   }

   private static void rawOpenFile(File file) throws Throwable {
      Desktop desktop = Desktop.getDesktop();
      desktop.open(file);
   }

   public static boolean openFile(File file, boolean showError) {
      try {
         rawOpenFile(file);
         return true;
      } catch (Throwable var3) {
         U.log("Cannot open file:", file);
         if (showError) {
            Alert.showError(Localizable.get("ui.error.openfile.title"), Localizable.get("ui.error.openfile", file), var3);
         }

         return false;
      }
   }

   public static boolean openFile(File file) {
      return openFile(file, true);
   }

   public int getRecommendedMemory() {
      return this.is32Bit() ? 512 : 1024;
   }

   public static String getCurrentInfo() {
      return System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + "Java " + System.getProperty("java.version");
   }
}
