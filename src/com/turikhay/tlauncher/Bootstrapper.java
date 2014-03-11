package com.turikhay.tlauncher;

import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.util.FileUtil;
import java.io.File;
import java.io.IOException;
import net.minecraft.launcher.process.JavaProcessLauncher;

public class Bootstrapper {
   public static void main() {
      main(new String[0]);
   }

   public static void main(String[] args) {
      System.out.println("TLauncher Bootstrapper is enabled.");
      JavaProcessLauncher launcher = createLauncher(args);

      try {
         launcher.start();
      } catch (Throwable var3) {
         Alert.showError("Cannot start TLauncher!", "Bootstrapper encountered an error. Please, contact developer: seventype@ya.ru", var3);
         TLauncher.main(args);
         return;
      }

      System.exit(0);
   }

   private static JavaProcessLauncher createLauncher(String[] args) {
      File file = FileUtil.getRunningJar();
      File dir = file.getParentFile();
      JavaProcessLauncher launcher = new JavaProcessLauncher((String)null, new String[0]);
      launcher.directory(dir);
      launcher.addCommand("-Xmx128m");
      launcher.addCommand("-cp", file.getAbsolutePath());
      launcher.addCommand("com.turikhay.tlauncher.TLauncher");
      launcher.addCommands(args);
      return launcher;
   }

   public static ProcessBuilder buildProcess(String[] args) throws IOException {
      return createLauncher(args).createProcess();
   }
}
