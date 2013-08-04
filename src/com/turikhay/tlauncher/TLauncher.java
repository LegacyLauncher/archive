package com.turikhay.tlauncher;

import LZMA.LzmaInputStream;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.DownloadableHandler;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Language;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.timer.Timer;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.LoginForm;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import javax.swing.JFrame;
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.updater.LocalVersionList;
import net.minecraft.launcher_.updater.RemoteVersionList;
import net.minecraft.launcher_.updater.VersionManager;

public class TLauncher extends Thread implements UpdaterListener {
   public static final double VERSION = 0.11D;
   private boolean isAvaiable = true;
   private String[] args;
   public final Settings settings;
   public final Settings lang;
   public final Downloader silent_downloader;
   public final Downloader downloader;
   public final Updater updater;
   public final TLauncherFrame frame;
   public final Timer timer;
   public final VersionManager vm;

   public TLauncher(String[] args) throws Exception {
      Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
      U.setWorkingTo(this);
      this.args = args;
      this.settings = new GlobalSettings();
      this.lang = new Language();
      this.silent_downloader = new Downloader("S", 1);
      this.downloader = new Downloader(10);
      this.updater = new Updater(this);
      this.timer = new Timer();
      this.vm = new VersionManager(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), new RemoteVersionList());
      this.frame = new TLauncherFrame(this);
      this.downloader.launch();
      this.init();
   }

   private void init() throws IOException {
      LoginForm lf = this.frame.getLoginForm();
      this.vm.addRefreshedVersionsListener(lf);
      this.vm.asyncRefresh();
      this.updater.addListener(this);
      this.updater.findUpdate();
   }

   public void launch(MinecraftLauncherListener listener, String username, String version, boolean forceupdate) {
      MinecraftLauncher launcher = new MinecraftLauncher(this, listener, version, forceupdate, username, this.args);
      launcher.run();
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
      this.isAvaiable = false;
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
         System.exit(0);
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

      U.log("Starting version 0.11...");
      TLauncher l = new TLauncher(args);
      l.start();
      U.log("Started!");

      while(l.isAvailable()) {
         try {
            Thread.sleep(500L);
         } catch (InterruptedException var3) {
            throw new TLauncherException("Runner cannot sleep.", var3);
         }
      }

      U.linelog("Good bye!");
   }

   public void onUpdaterRequesting(Updater u) {
      U.log("Searching for update...");
   }

   public void onUpdaterRequestError(Updater u, Throwable e) {
      U.log("Error occurred while getting update:", (Throwable)e);
   }

   public void onUpdaterFoundUpdate(Updater u, boolean canBeInstalledAutomatically) {
      double found_version = u.getFoundVersion();
      boolean yes = Alert.showQuestion(this.lang.get("updater.found.title"), this.lang.get("updater.found", "v", found_version), true);
      if (yes) {
         if (canBeInstalledAutomatically) {
            u.downloadUpdate();
         } else {
            OperatingSystem.openLink(u.getFoundLinkAsURI());
         }

      }
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
      U.log("No update found.");
   }

   public void onUpdaterDownloading(Updater u) {
   }

   public void onUpdaterDownloadSuccess(Updater u) {
      Alert.showWarning(this.lang.get("updater.downloaded.title"), this.lang.get("updater.downloaded"));

      try {
         u.saveUpdate();
      } catch (IOException var3) {
         Alert.showError(this.lang.get("updater.save-error.title"), this.lang.get("updater.save-error"), var3);
      }

   }

   public void onUpdaterDownloadError(Updater u, Throwable e) {
      Alert.showError(this.lang.get("updater.error.title"), this.lang.get("updater.error.title"), e);
   }
}
