package ru.turikhay.tlauncher;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.LoadingFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public final class Bootstrapper {
   private static final String MAIN_CLASS = "ru.turikhay.tlauncher.TLauncher";
   private static final int MAX_MEMORY = 128;
   private static final File DIRECTORY = new File(".");
   private final JavaProcessLauncher processLauncher;
   private final LoadingFrame frame;
   private final Bootstrapper.BootstrapperListener listener;
   private JavaProcess process;
   private boolean started;

   public static void main(String[] args) {
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
      processLauncher.addCommand("-Xmx128m");
      processLauncher.addCommand("-cp", FileUtil.getRunningJar());
      processLauncher.addCommand("ru.turikhay.tlauncher.TLauncher");
      if (args != null && args.length > 0) {
         processLauncher.addCommands(args);
      }

      if (loadAdditionalArgs) {
         File argsFile = new File(DIRECTORY, "tlauncher.args");
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
      this.frame = new LoadingFrame();
      this.frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            Bootstrapper.this.frame.dispose();
            Bootstrapper.this.die(0);
         }
      });
      this.listener = new Bootstrapper.BootstrapperListener((Bootstrapper.BootstrapperListener)null);
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
         log("Ni tebe, ni mne, haha!");
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

            Bootstrapper.this.frame.setProgress(step.percentage);
            if (step.percentage == 100) {
               Bootstrapper.this.started = true;
               Bootstrapper.this.frame.dispose();
               Bootstrapper.this.die(0);
            }
         }

      }

      public void onJavaProcessEnded(JavaProcess jp) {
         int exit;
         if ((exit = jp.getExitCode()) != 0) {
            Alert.showError("Error starting TLauncher", "TLauncher application was closed with illegal exit code (" + exit + "). See console:", this.buffer.toString());
         }

         Bootstrapper.this.die(exit);
      }

      public void onJavaProcessError(JavaProcess jp, Throwable e) {
      }

      // $FF: synthetic method
      BootstrapperListener(Bootstrapper.BootstrapperListener var2) {
         this();
      }
   }

   public static enum LoadingStep {
      INITALIZING(21),
      LOADING_CONFIGURATION(35),
      LOADING_CONSOLE(41),
      LOADING_MANAGERS(51),
      LOADING_WINDOW(62),
      PREPARING_MAINPANE(77),
      POSTINIT_GUI(82),
      REFRESHING_INFO(91),
      SUCCESS(100);

      public static final String LOADING_PREFIX = "[Loading]";
      public static final String LOADING_DELIMITER = " = ";
      private final int percentage;

      private LoadingStep(int percentage) {
         this.percentage = percentage;
      }

      public int getPercentage() {
         return this.percentage;
      }
   }
}
