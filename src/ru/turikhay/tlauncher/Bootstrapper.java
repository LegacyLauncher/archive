package ru.turikhay.tlauncher;

import java.io.File;
import java.io.IOException;
import net.minecraft.launcher.process.JavaProcessLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class Bootstrapper {
   public static void main() {
      main(new String[0]);
   }

   public static void main(String[] args) {
      U.setPrefix((String)null);
      U.log("TLauncher Bootstrapper is enabled.");
      String[] extraArgs = loadExtraArgs();
      if (extraArgs.length > 0) {
         args = U.extend(extraArgs, args);
      }

      JavaProcessLauncher launcher = createLauncher(args);

      try {
         launcher.start();
      } catch (Throwable var4) {
         Alert.showError("Cannot start TLauncher!", "Bootstrapper encountered an error. Please, contact developer: seventype@ya.ru", var4);
         TLauncher.main(args);
         return;
      }

      System.exit(0);
   }

   private static JavaProcessLauncher createLauncher(String[] args) {
      JavaProcessLauncher launcher = new JavaProcessLauncher((String)null, new String[0]);
      launcher.directory(TLauncher.getDirectory());
      launcher.addCommand("-Xmx128m");
      launcher.addCommand("-cp", FileUtil.getRunningJar());
      launcher.addCommand("ru.turikhay.tlauncher.TLauncher");
      launcher.addCommands(args);
      U.log("Process built:", launcher.getCommandsAsString());
      return launcher;
   }

   private static String[] loadExtraArgs() {
      String[] extraArgs = new String[0];
      File argsFile = FileUtil.getNeighborFile("tlauncher.args");
      if (!argsFile.isFile()) {
         return extraArgs;
      } else {
         U.log("TLauncher Bootstrapper is enabled.");

         String argsContent;
         try {
            argsContent = FileUtil.readFile(argsFile);
         } catch (IOException var4) {
            U.log("Cannot read file:", argsFile, var4);
            return extraArgs;
         }

         extraArgs = argsContent.split(" ");
         return extraArgs;
      }
   }

   public static ProcessBuilder buildProcess(String[] args) {
      return createLauncher(args).createProcess();
   }
}
