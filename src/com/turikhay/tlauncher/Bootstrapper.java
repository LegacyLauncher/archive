package com.turikhay.tlauncher;

import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.updater.PackageType;
import com.turikhay.tlauncher.util.FileUtil;
import java.io.File;
import net.minecraft.launcher_.process.JavaProcessLauncher;

public class Bootstrapper {
   public static void main(String[] args) {
      PackageType current = PackageType.getCurrent();
      if (current != PackageType.JAR) {
         System.out.println("TLauncher Bootstrapper is not required. Continuing...");
         TLauncher.main(args);
      } else {
         System.out.println("TLauncher Bootstrapper is enabled.");
         File file = FileUtil.getRunningJar();
         File dir = file.getParentFile();
         JavaProcessLauncher launcher = new JavaProcessLauncher((String)null, new String[0]);
         launcher.directory(dir);
         launcher.addCommand("-Xmx128m");
         launcher.addCommand("-cp", file);
         launcher.addCommand("com.turikhay.tlauncher.TLauncher");
         launcher.addCommands(args);
         System.out.println("Starting: " + launcher.getCommandsAsString());

         try {
            launcher.start();
         } catch (Throwable var6) {
            Alert.showError("Cannot start TLauncher!", "Bootstrapper encountered an error. Please, contact developer: seventype@ya.ru", var6);
            TLauncher.main(args);
            return;
         }

         System.out.println("Apllication output console is not redirecting. Stopping Bootstrapper.");
         System.exit(0);
      }
   }
}
