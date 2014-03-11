package com.turikhay.tlauncher.updater;

import com.turikhay.tlauncher.Bootstrapper;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.SimpleConfiguration;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableHandler;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.downloader.RetryDownloadException;
import com.turikhay.util.U;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Update {
   public static final int NONE = 0;
   public static final int DOWNLOADING = 1;
   public static final int DOWNLOADED = 2;
   public static final int UPDATING = 3;
   private int step;
   private double version;
   private String description;
   private Map links = new HashMap();
   private final Updater upd;
   private final Downloader d;
   private final List listeners = Collections.synchronizedList(new ArrayList());

   public void addListener(UpdateListener l) {
      this.listeners.add(l);
   }

   public void removeListener(UpdateListener l) {
      this.listeners.remove(l);
   }

   Update(Updater upd, Downloader d, double version, String description) {
      if (upd == null) {
         throw new NullPointerException("Updater is NULL!");
      } else if (d == null) {
         throw new NullPointerException("Downloader is NULL!");
      } else {
         this.upd = upd;
         this.d = d;
         this.setVersion(version);
         this.setDescription(description);
      }
   }

   Update(Updater upd, Downloader d, SimpleConfiguration settings) {
      if (upd == null) {
         throw new NullPointerException("Updater is NULL!");
      } else if (d == null) {
         throw new NullPointerException("Downloader is NULL!");
      } else if (settings == null) {
         throw new NullPointerException("Settings is NULL!");
      } else {
         this.upd = upd;
         this.d = d;
         this.setVersion(settings.getDouble("latest"));
         this.setDescription(settings.get("description"));
         Iterator var5 = settings.getKeys().iterator();

         while(var5.hasNext()) {
            String key = (String)var5.next();

            try {
               this.links.put(PackageType.valueOf(key.toUpperCase()), U.makeURI(settings.get(key)));
            } catch (Exception var7) {
            }
         }

         log("An update available for packages:", this.links.keySet());
      }
   }

   public Updater getUpdater() {
      return this.upd;
   }

   URI getDownloadLinkFor(PackageType pt) {
      return (URI)this.links.get(pt);
   }

   public URI getDownloadLink() {
      return this.getDownloadLinkFor(PackageType.getCurrent());
   }

   public double getVersion() {
      return this.version;
   }

   public String getDescription() {
      return this.description;
   }

   public int getStep() {
      return this.step;
   }

   public void download(boolean async) {
      this.downloadFor(PackageType.getCurrent(), async);
   }

   public void download() {
      this.download(false);
   }

   public void asyncDownload() {
      this.download(true);
   }

   void downloadFor(PackageType pt, boolean async) {
      try {
         this.downloadFor_(pt, async);
      } catch (Exception var4) {
         this.onUpdateError(var4);
      }

   }

   private void downloadFor_(PackageType pt, boolean async) throws Exception {
      if (this.step > 0) {
         throw new Update.IllegalStepException(this.step);
      } else {
         URI download_link = this.getDownloadLinkFor(pt);
         if (download_link == null) {
            throw new NullPointerException("Update for package \"" + pt + "\" is not found");
         } else {
            File destination = Updater.getUpdateFileFor(pt);
            destination.deleteOnExit();
            Downloadable downloadable = new Downloadable(download_link.toString(), destination);
            downloadable.addHandler(new DownloadableHandler() {
               public void onStart(Downloadable d) {
                  Update.this.onUpdateDownloading();
               }

               public void onComplete(Downloadable d) throws RetryDownloadException {
                  Update.this.step = 2;
                  Update.this.onUpdateReady();
               }

               public void onError(Downloadable d, Throwable e) {
                  Update.this.step = 0;
                  Update.this.onUpdateDownloadError(e);
               }

               public void onAbort(Downloadable d) {
                  Update.this.step = 0;
                  Update.this.onUpdateDownloadError(d.getError());
               }
            });
            this.d.add(downloadable);
            if (async) {
               this.d.startDownload();
            } else {
               this.d.startDownloadAndWait();
            }

         }
      }
   }

   public void apply() {
      this.applyFor(PackageType.getCurrent());
   }

   void applyFor(PackageType pt) {
      try {
         this.applyFor_(pt);
      } catch (Exception var3) {
         this.onUpdateApplyError(var3);
      }

   }

   private void applyFor_(PackageType pt) throws Exception {
      if (this.step < 2) {
         throw new Update.IllegalStepException(this.step);
      } else {
         log("Saving update... Launcher will be reopened.");
         File replace = Updater.getFileFor(pt);
         File replacer = Updater.getUpdateFileFor(pt);
         replacer.deleteOnExit();
         String[] args = TLauncher.getInstance() != null ? TLauncher.getArgs() : new String[0];
         ProcessBuilder builder = Bootstrapper.buildProcess(args);
         FileInputStream in = new FileInputStream(replacer);
         FileOutputStream out = new FileOutputStream(replace);
         this.onUpdateApplying();
         byte[] buffer = new byte[65536];

         for(int curread = in.read(buffer); curread > 0; curread = in.read(buffer)) {
            out.write(buffer, 0, curread);
         }

         in.close();
         out.close();

         try {
            builder.start();
         } catch (Exception var11) {
         }

         System.exit(0);
      }
   }

   void setVersion(double v) {
      if (v <= 0.0D) {
         throw new IllegalArgumentException("Invalid version!");
      } else {
         this.version = v;
      }
   }

   void setDescription(String desc) {
      this.description = desc;
   }

   void setLinkFor(PackageType pt, URI link) {
      if (pt == null) {
         throw new NullPointerException("PackageType is NULL!");
      } else if (link == null) {
         throw new NullPointerException("URI is NULL!");
      } else {
         if (this.links.containsKey(pt)) {
            this.links.remove(pt);
         }

         this.links.put(pt, link);
      }
   }

   private void onUpdateError(Throwable e) {
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdateListener l = (UpdateListener)var4.next();
            l.onUpdateError(this, e);
         }

      }
   }

   private void onUpdateDownloading() {
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdateListener l = (UpdateListener)var3.next();
            l.onUpdateDownloading(this);
         }

      }
   }

   private void onUpdateDownloadError(Throwable e) {
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdateListener l = (UpdateListener)var4.next();
            l.onUpdateDownloadError(this, e);
         }

      }
   }

   private void onUpdateReady() {
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdateListener l = (UpdateListener)var3.next();
            l.onUpdateReady(this);
         }

      }
   }

   private void onUpdateApplying() {
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdateListener l = (UpdateListener)var3.next();
            l.onUpdateApplying(this);
         }

      }
   }

   private void onUpdateApplyError(Throwable e) {
      U.log("Apply error");
      synchronized(this.listeners) {
         U.log(e);
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdateListener l = (UpdateListener)var4.next();
            l.onUpdateApplyError(this, e);
         }

      }
   }

   private static void log(Object... obj) {
      U.log("[Updater]", obj);
   }

   private static String getMessageForStep(int step, String description) {
      String r = "Illegal action on step #" + step;
      if (description != null) {
         r = r + " : " + description;
      }

      return r;
   }

   public class IllegalStepException extends RuntimeException {
      private static final long serialVersionUID = -1988019882288031411L;

      IllegalStepException(int step, String description) {
         super(Update.getMessageForStep(step, description));
      }

      IllegalStepException(int step) {
         super(Update.getMessageForStep(step, (String)null));
      }
   }
}
