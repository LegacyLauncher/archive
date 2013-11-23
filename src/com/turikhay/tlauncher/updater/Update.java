package com.turikhay.tlauncher.updater;

import com.turikhay.tlauncher.Bootstrapper;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Update {
   private int step;
   private double version;
   private String description;
   private Map links = new HashMap();
   private final Downloader d;
   private boolean isDownloading;
   private List listeners = new ArrayList();

   public void addListener(UpdateListener l) {
      this.listeners.add(l);
   }

   public void removeListener(UpdateListener l) {
      this.listeners.remove(l);
   }

   Update(Downloader d, double version, String description) {
      if (d == null) {
         throw new NullPointerException("Downloader is NULL!");
      } else {
         this.d = d;
         this.setVersion(version);
         this.setDescription(description);
      }
   }

   Update(Downloader d, Settings settings) {
      if (d == null) {
         throw new NullPointerException("Downloader is NULL!");
      } else if (settings == null) {
         throw new NullPointerException("Settings is NULL!");
      } else {
         this.d = d;
         this.setVersion(settings.getDouble("latest"));
         this.setDescription(settings.nget("description"));
         Iterator var4 = settings.getKeys().iterator();

         while(var4.hasNext()) {
            String key = (String)var4.next();

            try {
               this.links.put(PackageType.valueOf(key.toUpperCase()), U.makeURI(settings.nget(key)));
            } catch (Exception var6) {
            }
         }

         U.log(this.links);
      }
   }

   public URI getDownloadLinkFor(PackageType pt) {
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

   public void download() {
      this.downloadFor(PackageType.getCurrent());
   }

   public void downloadFor(PackageType pt) {
      try {
         this.downloadFor_(pt);
      } catch (Exception var3) {
         this.onUpdateError(var3);
      }

   }

   private void downloadFor_(PackageType pt) throws Exception {
      if (this.step > Update.Step.NONE.ordinal()) {
         throw new Update.IllegalStepException(this.step);
      } else {
         log(0);
         URI download_link = this.getDownloadLinkFor(pt);
         if (download_link == null) {
            throw new NullPointerException("Update for package \"" + pt + "\" is not found");
         } else {
            log(1);
            File destination = Updater.getUpdateFileFor(pt);
            destination.deleteOnExit();
            final Downloadable downloadable = new Downloadable(download_link.toURL(), destination);
            downloadable.addHandler(new DownloadableHandler() {
               public void onStart() {
               }

               public void onCompleteError() {
                  Update.this.isDownloading = false;
                  Update.this.step = Update.Step.NONE.ordinal();
                  Update.this.onUpdateDownloadError(downloadable.getError());
               }

               public void onComplete() {
                  Update.this.isDownloading = false;
                  Update.this.step = Update.Step.DOWNLOADED.ordinal();
                  Update.this.onUpdateReady();
               }
            });
            log(2);
            this.onUpdateDownloading();
            this.isDownloading = true;
            this.d.add(downloadable);
            this.d.launch();

            while(this.isDownloading) {
               U.sleepFor(1000L);
            }

            log(3);
         }
      }
   }

   public void apply() {
      this.applyFor(PackageType.getCurrent());
   }

   public void applyFor(PackageType pt) {
      try {
         this.applyFor_(pt);
      } catch (Exception var3) {
         this.onUpdateApplyError(var3);
      }

   }

   private void applyFor_(PackageType pt) throws Exception {
      if (this.step < Update.Step.DOWNLOADED.ordinal()) {
         throw new Update.IllegalStepException(this.step);
      } else {
         log("Saving update... Launcher will be reopened.");
         File replace = Updater.getFileFor(pt);
         File replacer = Updater.getUpdateFileFor(pt);
         replacer.deleteOnExit();
         String[] args = TLauncher.getInstance() != null ? TLauncher.getInstance().sargs : new String[0];
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
         builder.start();
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
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         UpdateListener l = (UpdateListener)var3.next();
         l.onUpdateError(this, e);
      }

   }

   private void onUpdateDownloading() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdateListener l = (UpdateListener)var2.next();
         l.onUpdateDownloading(this);
      }

   }

   private void onUpdateDownloadError(Throwable e) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         UpdateListener l = (UpdateListener)var3.next();
         l.onUpdateDownloadError(this, e);
      }

   }

   private void onUpdateReady() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdateListener l = (UpdateListener)var2.next();
         l.onUpdateReady(this);
      }

   }

   private void onUpdateApplying() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdateListener l = (UpdateListener)var2.next();
         l.onUpdateApplying(this);
      }

   }

   private void onUpdateApplyError(Throwable e) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         UpdateListener l = (UpdateListener)var3.next();
         l.onUpdateApplyError(this, e);
      }

   }

   private static void log(Object... obj) {
      U.log("[Updater]", obj);
   }

   private static String getMessageForStep(int step, String description) {
      String r = "Illegal action on step #" + step;
      Update.Step[] var6;
      int var5 = (var6 = Update.Step.values()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Update.Step curstep = var6[var4];
         if (curstep.ordinal() == step) {
            r = curstep.toString();
            break;
         }
      }

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

   public static enum Step {
      NONE,
      DOWNLOADING,
      DOWNLOADED,
      UPDATING;
   }
}
