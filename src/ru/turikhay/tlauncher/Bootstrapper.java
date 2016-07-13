package ru.turikhay.tlauncher;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.exceptions.TLauncherException;
import ru.turikhay.tlauncher.ui.LoadingFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public final class Bootstrapper {
   private static final File DIRECTORY = new File(".");
   private final JavaProcessLauncher processLauncher;
   private final LoadingFrame frame;
   private final Bootstrapper.BootstrapperListener listener;
   private JavaProcess process;
   private boolean terminate = true;
   private boolean started;

   public static void main(String[] args) {
      checkRunningPath();

      try {
         (new Bootstrapper(args)).start();
      } catch (IOException var2) {
         var2.printStackTrace();
         TLauncher.main(args);
      }

   }

   public static JavaProcessLauncher createLauncher(String[] args, boolean loadAdditionalArgs) {
      JavaProcessLauncher processLauncher = new JavaProcessLauncher((String)null, new String[0]);
      processLauncher.directory(DIRECTORY);
      processLauncher.addCommand("-Xmx176m");
      processLauncher.addCommand("-cp", FileUtil.getRunningJar());
      processLauncher.addCommand("ru.turikhay.tlauncher.TLauncher");
      if (args != null && args.length > 0) {
         processLauncher.addCommands(args);
      }

      if (loadAdditionalArgs) {
         File argsFile = new File(DIRECTORY, "tlauncher-" + OS.CURRENT.toString().toLowerCase() + "-" + OS.Arch.CURRENT.toString().toLowerCase() + ".args");
         if (!argsFile.isFile()) {
            argsFile = new File(DIRECTORY, "tlauncher.args");
         }

         if (argsFile.isFile()) {
            String[] extraArgs = loadArgsFromFile(argsFile);
            if (extraArgs != null) {
               processLauncher.addCommands(extraArgs);
            }
         }
      }

      return processLauncher;
   }

   public static JavaProcessLauncher createLauncher(String[] args) {
      return createLauncher(args, true);
   }

   public Bootstrapper(String[] args) {
      this.processLauncher = createLauncher(args);
      if (args != null && args.length > 0) {
         String[] var2 = args;
         int var3 = args.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String arg = var2[var4];
            if (arg != null && arg.startsWith("-") && arg.endsWith("no-terminate")) {
               log("Will only terminate when launcher is closed.");
               this.terminate = false;
               break;
            }
         }
      }

      this.frame = new LoadingFrame();
      this.frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            Bootstrapper.this.frame.dispose();
            Bootstrapper.this.die(0);
         }
      });
      this.listener = new Bootstrapper.BootstrapperListener();
   }

   public void start() throws IOException {
      if (this.process != null) {
         throw new IllegalStateException("Process is already started");
      } else {
         log("Starting launcher...");
         this.process = this.processLauncher.start();
         this.process.safeSetExitRunnable(this.listener);
         this.frame.setTitle("TLauncher " + TLauncher.getVersion());
         this.frame.setVisible(true);
      }
   }

   private void die(int status) {
      log("I can be terminated now.");
      if (!this.started && this.process.isRunning()) {
         log("...started instance also will be terminated.");
         log("Poka!");
         this.process.stop();
      }

      System.exit(status);
   }

   private static String[] loadArgsFromFile(File file) {
      log("Loading arguments from file:", file);

      String content;
      try {
         content = FileUtil.readFile(file);
      } catch (IOException var3) {
         log("Cannot load arguments from file:", file);
         return null;
      }

      return StringUtils.split(content, ' ');
   }

   private static void log(Object... s) {
      U.log("[Bootstrapper]", s);
   }

   public static void checkRunningPath() {
      String path = Bootstrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      if (path.contains("!")) {
         String message = "Please do not run (any) Java application which path contains folder name that ends with «!»\nНе запускайте Java-приложения в директориях, чей путь содержит «!»\n\n" + path;
         JOptionPane.showMessageDialog((Component)null, message, "Error", 0);
         throw new TLauncherException(message);
      }
   }

   public static enum LoadingStep {
      INITALIZING(11),
      LOADING_CONFIGURATION(25),
      LOADING_LOOKANDFEEL(35),
      LOADING_LOGGER(40),
      LOADING_FIRSTRUN(45),
      LOADING_MANAGERS(50),
      LOADING_WINDOW(62),
      PREPARING_MAINPANE(77),
      POSTINIT_GUI(82),
      REFRESHING_INFO(91),
      SUCCESS(100);

      private final int percentage;

      private LoadingStep(int percentage) {
         this.percentage = percentage;
      }
   }

   private class BootstrapperListener implements JavaProcessListener {
      private StringBuffer buffer;

      private BootstrapperListener() {
         this.buffer = new StringBuffer();
      }

      public void onJavaProcessLog(JavaProcess jp, String line) {
         U.plog('>', line);
         this.buffer.append(line).append('\n');
         if (line.startsWith("[Loading]")) {
            if (line.length() < "[Loading]".length() + 2) {
               Bootstrapper.log("Cannot parse line: content is empty.");
               return;
            }

            String content = line.substring("[Loading]".length() + 1);
            Bootstrapper.LoadingStep step = (Bootstrapper.LoadingStep)Reflect.parseEnum(Bootstrapper.LoadingStep.class, content);
            if (step == null) {
               Bootstrapper.log("Cannot parse line: cannot parse step");
               return;
            }

            if (step == Bootstrapper.LoadingStep.LOADING_FIRSTRUN) {
               Bootstrapper.this.frame.setExtendedState(1);
            } else if (Bootstrapper.this.frame.getExtendedState() != 1) {
               Bootstrapper.this.frame.setExtendedState(0);
            }

            Bootstrapper.this.frame.setProgress(step.percentage);
            if (step.percentage == 100) {
               Bootstrapper.this.started = true;
               Bootstrapper.this.frame.dispose();
               if (Bootstrapper.this.terminate) {
                  Bootstrapper.this.die(0);
               }
            }
         }

      }

      public void onJavaProcessEnded(JavaProcess jp) {
         if (!Bootstrapper.this.frame.isVisible()) {
            Bootstrapper.this.frame.setVisible(true);
         }

         int exit = jp.getExitCode();
         if (exit != 0) {
            if ("Error: Could not find or load main class".equals(this.buffer.substring(0, "Error: Could not find or load main class".length()))) {
               JOptionPane.showMessageDialog((Component)null, "Could not find or load main class. You can try to place executable file into root folder or download launcher once more using link below.\nНе удалось загрузить главный класс Java. Попробуйте положить TLauncher в корневую директорию (например, в C:\\) или загрузить\nлаунчер заново, используя ссылку ниже.\n\nhttp://tlaun.ch/jar", "Error launching TLauncher", 0);
            } else {
               Alert.showError("Error starting TLauncher", "TLauncher application was closed with illegal exit code (" + exit + "). See logger:", this.buffer.toString());
            }
         }

         Bootstrapper.this.die(exit);
      }

      // $FF: synthetic method
      BootstrapperListener(Object x1) {
         this();
      }
   }
}
