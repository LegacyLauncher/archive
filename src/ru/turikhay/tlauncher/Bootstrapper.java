package ru.turikhay.tlauncher;

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
      JavaProcessLauncher launcher = new JavaProcessLauncher((String)null, new String[0]);
      launcher.directory(TLauncher.getDirectory());
      launcher.addCommand("-Xmx128m");
      launcher.addCommand("-cp", FileUtil.getRunningJar());
      launcher.addCommand("ru.turikhay.tlauncher.TLauncher");
      launcher.addCommands(args);
      U.log("Process built:", launcher.getCommandsAsString());
      return launcher;
   }

   public static ProcessBuilder buildProcess(String[] args) throws IOException {
      return createLauncher(args).createProcess();
   }
}
