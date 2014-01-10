package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.exceptions.TLauncherException;
import com.turikhay.tlauncher.minecraft.events.ProfileListener;
import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.util.U;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import net.minecraft.launcher.OperatingSystem;

public class TLauncherFrame extends JFrame implements ProfileListener, DownloadListener, UpdaterListener, UpdateListener {
   public static final Color backgroundColor = new Color(141, 189, 233);
   private final TLauncherFrame instance = this;
   final TLauncher t;
   private static final long serialVersionUID = 5949683935156305416L;
   private static List favicons = new ArrayList();
   int width;
   int height;
   Image bgimage;
   Image sun;
   GlobalSettings global;
   Settings lang;
   Downloader d;
   MainPane mp;
   ProgressBar pb;
   LoginForm lf;
   SettingsForm sf;
   ProfileCreatorForm spcf;
   ConnectionWarning warning;
   private boolean pb_started;
   private ProfileManager pm;

   public TLauncherFrame(TLauncher tlauncher) {
      this.t = tlauncher;
      this.global = this.t.getSettings();
      this.lang = this.t.getLang();
      this.d = this.t.getDownloader();
      this.pm = this.t.getCurrentProfileManager();

      try {
         this.loadResources();
      } catch (Exception var3) {
         throw new TLauncherException("Cannot load required resource!", var3);
      }

      int[] w_sizes = this.global.getWindowSize();
      this.width = w_sizes[0];
      this.height = w_sizes[1];
      log("Preparing main frame...");
      this.prepareFrame();
      this.setVisible(true);
      this.requestFocusInWindow();
      if (this.global.isFirstRun()) {
         Alert.showAsyncWarning(this.lang.get("firstrun.title"), U.w(this.lang.get("firstrun"), 90));
      }

      this.d.addListener(this);
   }

   public void resizeWindow(int w, int h) {
      Dimension sizes = new Dimension(this.width = w, this.height = h);
      this.setPreferredSize(sizes);
      this.setMinimumSize(sizes);
      this.setLocationRelativeTo((Component)null);
      this.setLayout(new BorderLayout());
   }

   private void initFontSize() {
      try {
         UIDefaults defaults = UIManager.getDefaults();
         Enumeration e = defaults.keys();

         while(e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font) {
               Font font = (Font)value;
               int newSize = Math.round((float)(font.getSize() + 1));
               if (value instanceof FontUIResource) {
                  defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), newSize));
               } else {
                  defaults.put(key, new Font(font.getName(), font.getStyle(), newSize));
               }
            }
         }
      } catch (Exception var7) {
         log("Cannot change font sizes!", var7);
      }

   }

   private void initUILocale() {
      UIManager.put("OptionPane.yesButtonText", this.lang.nget("ui.yes"));
      UIManager.put("OptionPane.noButtonText", this.lang.nget("ui.no"));
      UIManager.put("OptionPane.cancelButtonText", this.lang.nget("ui.cancel"));
   }

   public void updateLocales() {
      try {
         this.t.reloadLocale();
      } catch (Exception var2) {
         log("Cannot reload settings!", var2);
         return;
      }

      Console.updateLocale();
      this.setWindowTitle();
      this.initUILocale();
      updateContainer(this, true);
   }

   public static void updateContainer(Container container, boolean deep) {
      Component[] var5;
      int var4 = (var5 = container.getComponents()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         Component c = var5[var3];
         if (c instanceof LocalizableComponent) {
            ((LocalizableComponent)c).updateLocale();
         }

         if (c instanceof Container && deep) {
            updateContainer((Container)c, true);
         }
      }

   }

   public static void initLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var1) {
         log("Can't set system look and feel.");
         var1.printStackTrace();
      }

   }

   public static List getFavicons() {
      if (!favicons.isEmpty()) {
         return favicons;
      } else {
         int[] sizes = new int[]{256, 128, 96, 64, 48, 32, 24, 16};

         try {
            int[] var4 = sizes;
            int var3 = sizes.length;

            for(int var2 = 0; var2 < var3; ++var2) {
               int i = var4[var2];
               favicons.add(ImageIO.read(TLauncherFrame.class.getResource("fav" + i + ".png")));
            }
         } catch (IOException var5) {
            log("Cannot load favicon. Where is it?", var5);
            return null;
         }

         return favicons;
      }
   }

   private void prepareFrame() {
      initLookAndFeel();
      this.initUILocale();
      this.setBackground(backgroundColor);
      this.initFontSize();
      this.setWindowTitle();
      this.resizeWindow(this.width, this.height);
      this.setIconImages(getFavicons());
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            TLauncherFrame.this.instance.setVisible(false);
            TLauncher.kill();
         }
      });
      this.addComponentListener(new ComponentListener() {
         public void componentResized(ComponentEvent e) {
            TLauncherFrame.this.mp.onResize();
         }

         public void componentShown(ComponentEvent e) {
            TLauncherFrame.log("Hi, guys!");
            TLauncherFrame.this.mp.startBackground();
            TLauncherFrame.this.instance.validate();
            TLauncherFrame.this.instance.repaint();
            TLauncherFrame.this.instance.toFront();
         }

         public void componentMoved(ComponentEvent e) {
         }

         public void componentHidden(ComponentEvent e) {
            TLauncherFrame.this.mp.suspendBackground();
         }
      });
      log("Preparing components...");
      this.sf = new SettingsForm(this);
      this.lf = new LoginForm(this);
      this.spcf = new ProfileCreatorForm(this);
      this.pb = new ProgressBar(this);
      this.warning = new ConnectionWarning();
      log("Preparing main pane...");
      this.mp = new MainPane(this);
      this.add(this.mp);
      this.add("South", this.pb);
      log("Packing main frame...");
      this.pack();
      log("Resizing main pane...");
      this.mp.onResize();
   }

   private void loadResources() throws IOException {
      this.bgimage = ImageIO.read(TLauncherFrame.class.getResource("grass.png"));
      this.sun = ImageIO.read(TLauncherFrame.class.getResource("sun.png"));
   }

   private void setWindowTitle() {
      String translator = this.lang.nget("translator");
      this.setTitle("TLauncher " + TLauncher.getVersion() + " (by turikhay" + (translator != null ? ", translated by " + translator : "") + ")");
   }

   public LoginForm getLoginForm() {
      return this.lf;
   }

   public ProgressBar getProgressBar() {
      return this.pb;
   }

   public ProfileManager getProfileManager() {
      return this.pm;
   }

   public ConnectionWarning getConnectionWarning() {
      return this.warning;
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

   public void onDownloaderAbort(Downloader d) {
      this.pb_started = false;
      this.pb.progressStop();
   }

   public void onDownloaderComplete(Downloader d) {
      this.pb_started = false;
      this.pb.progressStop();
   }

   public void onDownloaderFileComplete(Downloader d, Downloadable f) {
      this.pb.setIndeterminate(false);
      this.pb.setWestString(this.lang.get("progressBar.completed", "f", f.getFilename()));
      this.pb.setEastString(this.lang.get("progressBar.remaining", "i", d.getRemaining()));
   }

   public void onDownloaderError(Downloader d, Downloadable file, Throwable error) {
      int i = d.getRemaining();
      if (i == 0) {
         this.onDownloaderComplete(d);
      } else {
         String path = "download.error" + (error == null ? ".unknown" : "");
         this.pb.setIndeterminate(false);
         this.pb.setCenterString(this.lang.get(path, "f", file.getFilename(), "e", error.toString()));
      }
   }

   public void onDownloaderProgress(Downloader d, int progress, double speed) {
      if (progress > 0) {
         if (this.pb.getValue() > progress) {
            return;
         }

         this.pb.setIndeterminate(false);
         this.pb.setValue(progress);
         this.pb.setCenterString(progress + "%");
      }

   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdaterRequestError(Updater u) {
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onUpdateFound(Updater u, Update upd) {
      if (!this.t.isLauncherWorking()) {
         double version = upd.getVersion();
         Alert.showWarning(this.lang.get("updater.found.title"), this.lang.get("updater.found", "v", version), upd.getDescription());
         if (Updater.isAutomode()) {
            upd.addListener(this);
            upd.download();
         } else {
            if (this.openUpdateLink(upd.getDownloadLink())) {
               TLauncher.kill();
            }

         }
      }
   }

   public void onUpdateError(Update u, Throwable e) {
      if (Alert.showQuestion(this.lang.get("updater.error.title"), this.lang.get("updater.download-error"), e, true)) {
         this.openUpdateLink(u.getDownloadLink());
      }

   }

   public void onUpdateDownloading(Update u) {
   }

   public void onUpdateDownloadError(Update u, Throwable e) {
      this.onUpdateError(u, e);
   }

   public void onUpdateReady(Update u) {
      Alert.showWarning(this.lang.get("updater.downloaded.title"), this.lang.get("updater.downloaded"));
      u.apply();
   }

   public void onUpdateApplying(Update u) {
   }

   public void onUpdateApplyError(Update u, Throwable e) {
      if (Alert.showQuestion(this.lang.get("updater.save-error.title"), this.lang.get("updater.save-error"), e, true)) {
         this.openUpdateLink(u.getDownloadLink());
      }

   }

   private boolean openUpdateLink(URI uri) {
      try {
         OperatingSystem.openLink(uri);
         return true;
      } catch (Exception var3) {
         Alert.showError(this.lang.get("updater.found.cannotopen.title"), this.lang.get("updater.found.cannotopen"), (Object)uri);
         return false;
      }
   }

   public void onAdFound(Updater u, Ad ad) {
      if (this.global.getInteger("updater.ad") != ad.getID()) {
         if (ad.canBeShown()) {
            this.global.set("updater.ad", ad.getID());
            ad.show(false);
         }
      }
   }

   public void onProfilesRefreshed(ProfileManager pm) {
   }

   public void onProfileManagerChanged(ProfileManager pm) {
      this.pm = pm;
   }

   private static void log(Object... o) {
      U.log("[Frame]", o);
   }
}
