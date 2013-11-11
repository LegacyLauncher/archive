package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.exceptions.TLauncherException;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.tlauncher.util.Console;
import com.turikhay.tlauncher.util.U;
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
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import net.minecraft.launcher_.OperatingSystem;

public class TLauncherFrame extends JFrame implements DownloadListener, UpdaterListener, UpdateListener {
   public static final Color backgroundColor = new Color(141, 189, 233);
   private final TLauncherFrame instance = this;
   final TLauncher t;
   private static final long serialVersionUID = 5949683935156305416L;
   int width;
   int height;
   Image bgimage;
   Image favicon;
   Image sun;
   GlobalSettings global;
   Settings lang;
   Downloader d;
   MainPane mp;
   ProgressBar pb;
   LoginForm lf;
   SettingsForm sf;
   private boolean pb_started;

   public TLauncherFrame(TLauncher tlauncher) {
      this.t = tlauncher;
      this.global = this.t.getSettings();
      this.lang = this.t.getLang();
      this.d = this.t.getDownloader();

      try {
         this.loadResources();
      } catch (Exception var9) {
         throw new TLauncherException("Cannot load required resource!", var9);
      }

      int[] w_sizes = this.global.getWindowSize();
      this.width = w_sizes[0];
      this.height = w_sizes[1];
      long start = System.currentTimeMillis();
      U.log("Preparing main frame...");
      this.prepareFrame();
      long end = System.currentTimeMillis();
      long diff = end - start;
      U.log("Prepared:", diff);
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

   }

   public void updateLocales() {
      try {
         this.t.reloadLocale();
      } catch (Exception var2) {
         U.log("Cannot reload settings!", var2);
         return;
      }

      Console.updateLocale();
      this.setWindowTitle();
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

   private void prepareFrame() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var7) {
         U.log("Can't set system look and feel.");
         var7.printStackTrace();
      }

      this.setBackground(backgroundColor);
      this.initFontSize();
      this.setWindowTitle();
      this.resizeWindow(this.width, this.height);
      this.setIconImage(this.favicon);
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

         public void componentMoved(ComponentEvent e) {
         }

         public void componentShown(ComponentEvent e) {
         }

         public void componentHidden(ComponentEvent e) {
         }
      });
      long start = System.currentTimeMillis();
      U.log("Preparing components...");
      this.sf = new SettingsForm(this);
      this.lf = new LoginForm(this);
      this.pb = new ProgressBar(this);
      start = System.currentTimeMillis();
      U.log("Preparing main pane...");
      this.mp = new MainPane(this);
      long end = System.currentTimeMillis();
      long diff = end - start;
      U.log("Prepared pane:", diff);
      this.add(this.mp);
      this.add("South", this.pb);
      end = System.currentTimeMillis();
      diff = end - start;
      U.log("Components prepared:", diff);
      start = System.currentTimeMillis();
      U.log("Packing main frame...");
      this.pack();
      end = System.currentTimeMillis();
      diff = end - start;
      U.log("Packed:", diff);
      start = System.currentTimeMillis();
      U.log("Resizing main pane...");
      this.mp.onResize();
      end = System.currentTimeMillis();
      diff = end - start;
      U.log("Main pane resized:", diff);
   }

   private void loadResources() throws IOException {
      this.bgimage = ImageIO.read(TLauncherFrame.class.getResource("grass.png"));
      this.favicon = ImageIO.read(TLauncherFrame.class.getResource("favicon.png"));
      this.sun = ImageIO.read(TLauncherFrame.class.getResource("sun.png"));
   }

   private void setWindowTitle() {
      String translator = this.lang.nget("translator");
      this.setTitle("TLauncher 0.198 (by turikhay" + (translator != null ? ", translated by " + translator : "") + ")");
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

   public void onUpdaterRequestError(Updater u) {
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onUpdateFound(Updater u, Update upd) {
      double version = upd.getVersion();
      Alert.showWarning(this.lang.get("updater.found.title"), this.lang.get("updater.found", "v", version), upd.getDescription());
      if (Updater.isAutomode()) {
         upd.addListener(this);
         upd.download();
      } else {
         URI uri = upd.getDownloadLink();

         try {
            OperatingSystem.openLink(uri);
         } catch (Exception var7) {
            Alert.showError(this.lang.get("updater.found.cannotopen.title"), this.lang.get("updater.found.cannotopen"), (Object)uri);
         }

      }
   }

   public void onUpdateError(Update u, Throwable e) {
      Alert.showError(this.lang.get("updater.error.title"), this.lang.get("updater.error.title"), e);
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
      Alert.showError(this.lang.get("updater.save-error.title"), this.lang.get("updater.save-error"), e);
   }

   public void onAdFound(Updater u, Ad ad) {
      if (ad.canBeShown()) {
         this.global.set("updater.ad", ad.getID());
         ad.show(false);
      }
   }
}
