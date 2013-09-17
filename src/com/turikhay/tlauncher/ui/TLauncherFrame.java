package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.timer.Timer;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.tlauncher.util.U;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class TLauncherFrame extends JFrame implements DownloadListener, UpdaterListener {
   private final TLauncherFrame instance = this;
   final TLauncher t;
   private static final long serialVersionUID = 5949683935156305416L;
   int width;
   int height;
   Color bgcolor = new Color(141, 189, 233);
   Image bgimage;
   Image favicon;
   Image sun;
   Settings global;
   Settings lang;
   Downloader d;
   Timer ti;
   MainContainer mc;
   ProgressBar pb;
   LoginForm lf;
   SettingsForm sf;
   private boolean pb_started;

   public TLauncherFrame(TLauncher tlauncher) {
      super("TLauncher 0.166 (by turikhay)");
      this.t = tlauncher;
      this.global = this.t.settings;
      this.lang = this.t.lang;
      this.d = this.t.downloader;
      this.ti = this.t.timer;

      try {
         this.loadResources();
      } catch (Exception var3) {
         throw new TLauncherException("Cannot load required resource!", var3);
      }

      this.width = this.global.getInteger("minecraft.size.width");
      this.height = this.global.getInteger("minecraft.size.height");
      this.prepareFrame();
      this.setVisible(true);
      this.requestFocusInWindow();
      if (GlobalSettings.firstRun) {
         Alert.showAsyncWarning(this.lang.get("firstrun.title"), U.w(this.lang.get("firstrun"), 90));
      }

      this.d.addListener(this);
      this.ti.start();
   }

   public void resizeWindow(int w, int h) {
      Dimension sizes = new Dimension(this.width = w, this.height = h);
      this.setPreferredSize(sizes);
      this.setMinimumSize(sizes);
      this.setLocationRelativeTo((Component)null);
      this.setLayout(new BorderLayout());
   }

   private void prepareFrame() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var2) {
         U.log("Can't set system look and feel.");
         var2.printStackTrace();
      }

      this.resizeWindow(this.width, this.height);
      this.setIconImage(this.favicon);
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            TLauncherFrame.this.instance.setVisible(false);
            TLauncherFrame.this.t.kill();
         }
      });
      this.sf = new SettingsForm(this);
      this.lf = new LoginForm(this);
      this.pb = new ProgressBar(this);
      this.mc = new MainContainer(this);
      this.add(this.mc);
      this.add("South", this.pb);
      this.pack();
   }

   private void loadResources() throws IOException {
      this.bgimage = ImageIO.read(TLauncherFrame.class.getResource("grass.png"));
      this.favicon = ImageIO.read(TLauncherFrame.class.getResource("favicon.png"));
      this.sun = ImageIO.read(TLauncherFrame.class.getResource("sun.png"));
   }

   public LoginForm getLoginForm() {
      return this.lf;
   }

   public ProgressBar getProgressBar() {
      return this.pb;
   }

   public void onDownloaderStart(Downloader d, int files) {
      if (!this.pb_started) {
         this.pb_started = true;
         this.pb.progressStart();
         this.pb.setIndeterminate(true);
         this.pb.setCenterString(this.lang.get("progressBar.init"));
         this.pb.setEastString(this.lang.get("progressBar.downloading" + (files == 1 ? "-one" : ""), "i", files));
      }
   }

   public void onDownloaderComplete(Downloader d) {
      this.pb_started = false;
      this.pb.progressStop();
   }

   public void onDownloaderFileComplete(Downloader d, Downloadable f) {
      int i = d.getRemaining();
      this.pb.setEastString(this.lang.get("progressBar.remaining" + (i == 1 ? "-one" : ""), "i", i));
      this.pb.setWestString(this.lang.get("progressBar.completed", "f", f.getFilename()));
   }

   public void onDownloaderError(Downloader d, Downloadable file, Throwable error) {
      String path = "download.error" + (error == null ? ".unknown" : "");
      this.pb.setIndeterminate(false);
      this.pb.setCenterString(this.lang.get(path, "f", file.getFilename(), "e", error.toString()));
      this.pb.setWestString((String)null);
      this.pb.setEastString((String)null);
   }

   public void onDownloaderProgress(Downloader d, int progress) {
      if (progress > 0) {
         this.pb.setIndeterminate(false);
         this.pb.setValue(progress);
         this.pb.setCenterString(progress + "%");
      } else {
         this.pb.setIndeterminate(true);
         this.pb.setCenterString((String)null);
      }

   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdaterRequestError(Updater u, Throwable e) {
      U.log("Error occurred while getting update:", e);
   }

   public void onUpdaterFoundUpdate(Updater u) {
      double found_version = u.getFoundVersion();
      if (this.global.getDouble("updater.disallow") == found_version) {
         U.log("User cancelled updating to this version.");
      } else {
         boolean yes = Alert.showQuestion(this.lang.get("updater.found.title"), this.lang.get("updater.found", "v", found_version), true);
         if (yes) {
            u.downloadUpdate();
         } else {
            U.log("You don't want to update? Oh, okay... I will not disturb you with this version anymore.");
            this.global.set("updater.disallow", found_version);
         }
      }
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onUpdaterDownloading(Updater u) {
   }

   public void onUpdaterDownloadSuccess(Updater u) {
      Alert.showWarning(this.lang.get("updater.downloaded.title"), this.lang.get("updater.downloaded"));
      this.global.set("gui.sun", true);
      u.saveUpdate();
   }

   public void onUpdaterDownloadError(Updater u, Throwable e) {
      Alert.showError(this.lang.get("updater.error.title"), this.lang.get("updater.error.title"), e);
   }

   public void onUpdaterProcessError(Updater u, Throwable e) {
      Alert.showError(this.lang.get("updater.save-error.title"), this.lang.get("updater.save-error"), e);
   }
}
