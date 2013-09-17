package com.turikhay.tlauncher;

import LZMA.LzmaInputStream;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.timer.Timer;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.LoginForm;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Locale;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import javax.swing.JFrame;
import net.minecraft.launcher_.updater.VersionManager;

public class TLauncher {
   public static final double VERSION = 0.166D;
   public static final Locale DEFAULT_LOCALE;
   public static final String[] SUPPORTED_LOCALE;
   public final Locale locale;
   private static TLauncher instance;
   private boolean isAvaiable = true;
   private String[] args;
   public final Settings lang;
   public final GlobalSettings settings;
   public final Downloader downloader;
   public final Updater updater;
   public final TLauncherFrame frame;
   public final Timer timer;
   public final VersionManager vm;

   static {
      DEFAULT_LOCALE = Locale.US;
      SUPPORTED_LOCALE = new String[]{"ru_RU", "en_US"};
   }

   public TLauncher(String[] args) throws Exception {
      long start = System.currentTimeMillis();
      instance = this;
      Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
      U.setWorkingTo(this);
      this.args = args;
      this.settings = new GlobalSettings();
      this.locale = this.settings.getLocale();
      U.log("Selected locale: " + this.locale);
      this.lang = new Settings(TLauncher.class.getResource("/lang/" + this.locale + ".ini"));
      Alert.prepareLocal();
      this.downloader = new Downloader(10);
      this.updater = new Updater(this);
      this.timer = new Timer();
      this.vm = new VersionManager();
      this.frame = new TLauncherFrame(this);
      this.downloader.launch();
      this.init();
      long end = System.currentTimeMillis();
      long diff = end - start;
      U.log("Started! (" + diff + " ms.)");
   }

   private void init() throws IOException {
      LoginForm lf = this.frame.getLoginForm();
      this.vm.addRefreshedListener(lf.versionchoice);
      if (lf.autologin.isEnabled()) {
         this.vm.refreshVersions(true);
         lf.autologin.startLogin();
      } else {
         this.vm.asyncRefresh();
         this.vm.asyncRefreshResources();
      }

      this.updater.addListener(this.frame);
      this.updater.findUpdate();
      U.gc();
   }

   public void launch(MinecraftLauncherListener listener, boolean forceupdate) {
      MinecraftLauncher launcher = new MinecraftLauncher(this, listener, this.args, forceupdate);
      launcher.start();
   }

   public void runDefaultLauncher() {
      Class[] classes = new Class[]{JFrame.class, File.class, Proxy.class, PasswordAuthentication.class, String[].class, Integer.class};
      Object[] objects = new Object[]{this.frame, MinecraftUtil.getWorkingDirectory(), Proxy.NO_PROXY, null, new String[0], 5};
      MinecraftUtil.startLauncher(MinecraftUtil.getFile("launcher.jar"), classes, objects);
   }

   public void createDefaultLauncher(final boolean run) {
      DownloadableContainer c = new DownloadableContainer();
      final Downloadable d = MinecraftUtil.getDownloadable("https://s3.amazonaws.com/Minecraft.Download/launcher/launcher.pack.lzma", false);
      c.setHandler(new DownloadableHandler() {
         public void onComplete() {
            try {
               LzmaInputStream in = new LzmaInputStream(new FileInputStream(d.getDestination()));
               FileOutputStream out = new FileOutputStream(MinecraftUtil.getFile("launcher.pack"));
               byte[] buffer = new byte[65536];

               for(int read = in.read(buffer); read >= 1; read = in.read(buffer)) {
                  out.write(buffer, 0, read);
               }

               in.close();
               out.close();
               JarOutputStream jarOutputStream = null;
               jarOutputStream = new JarOutputStream(new FileOutputStream(MinecraftUtil.getFile("launcher.jar")));
               Pack200.newUnpacker().unpack(MinecraftUtil.getFile("launcher.pack"), jarOutputStream);
               jarOutputStream.close();
               if (run) {
                  TLauncher.this.runDefaultLauncher();
               }
            } catch (Exception var6) {
               var6.printStackTrace();
            }

         }

         public void onCompleteError() {
            TLauncher.this.frame.getLoginForm().cancelLogin();
         }

         public void onStart() {
         }
      });
      c.add(d);
      this.downloader.add(c);
      this.downloader.launch();
   }

   public boolean isAvailable() {
      return this.isAvaiable;
   }

   public void kill() {
      System.exit(0);
   }

   public void hide() {
      U.log("Hiding...");
      this.frame.setVisible(false);
   }

   public void show() {
      U.log("Here I am!");
      this.frame.setVisible(true);
   }

   public static void main(String[] args) {
      ExceptionHandler handler = new ExceptionHandler();
      Thread.setDefaultUncaughtExceptionHandler(handler);

      try {
         launch(args);
      } catch (Throwable var3) {
         var3.printStackTrace();
         Alert.showError(var3, true);
      }

   }

   private static void launch(String[] args) throws Exception {
      U.log("Hello!");
      if (args.length > 0) {
         U.log("All arguments will be passed in Minecraft directly");
      }

      U.log("Starting version 0.166...");
      new TLauncher(args);
   }

   public static TLauncher getInstance() {
      if (instance != null) {
         return instance;
      } else {
         throw new TLauncherException("Instance is not defined!");
      }
   }

   public static Locale getSupported() {
      Locale using = Locale.getDefault();
      String using_name = using.toString();
      String[] var5;
      int var4 = (var5 = SUPPORTED_LOCALE).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         String supported = var5[var3];
         if (supported.equals(using_name)) {
            return using;
         }
      }

      return Locale.US;
   }
}
