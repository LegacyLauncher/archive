package net.minecraft.launcher;

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

   public String getJavaDir(boolean console) {
      String separator = System.getProperty("file.separator");
      String path = System.getProperty("java.home") + separator + "bin" + separator;
      if (getCurrentPlatform() == WINDOWS) {
         String exec = console ? "java.exe" : "javaw.exe";
         File file = new File(path, exec);
         if (file.isFile()) {
            return path + exec;
         }
      }

      return path + "java";
   }

   public String getJavaDir() {
      return this.getJavaDir(false);
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

   public static boolean openLink(URI link) {
      if (!Desktop.isDesktopSupported()) {
         return false;
      } else {
         Desktop desktop = Desktop.getDesktop();

         try {
            desktop.browse(link);
            return true;
         } catch (Exception var3) {
            U.log("Failed to open link: " + link);
            return false;
         }
      }
   }

   public static boolean openFile(File file) {
      if (!Desktop.isDesktopSupported()) {
         return false;
      } else {
         Desktop desktop = Desktop.getDesktop();

         try {
            desktop.open(file);
            return true;
         } catch (Exception var3) {
            U.log("Failed to open file: " + file);
            return false;
         }
      }
   }

   public int getRecommendedMemory() {
      return this.is32Bit() ? 512 : 1024;
   }
}
