package com.turikhay.tlauncher.updater;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.exceptions.TLauncherException;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import wrapper.Wrapper;

public class Updater {
   public final String link = "http://u.to/tlauncher-update/ixhQBA";
   public boolean enabled;
   public Updater.Package type;
   private Downloader d;
   private URL url;
   private List listeners = new ArrayList();
   private Downloadable update_download;
   private Settings update_settings;
   private double found_version;
   private String found_link;
   private Downloadable launcher_download;
   private File launcher_destination;
   private File replace;

   public Updater(TLauncher t) {
      this.d = t.getDownloader();
      this.type = Wrapper.isAvailable() ? Updater.Package.EXE : Updater.Package.JAR;
      this.replace = Wrapper.isAvailable() ? Wrapper.getExecutable() : FileUtil.getRunningJar();

      try {
         this.url = new URL("http://u.to/tlauncher-update/ixhQBA");
      } catch (MalformedURLException var3) {
         throw new TLauncherException("Cannot create update link!", var3);
      }

      this.enabled = t.getSettings().getBoolean("updater.enabled");
      this.log("Package type: " + this.type);
   }

   public Updater(TLauncher t, Updater.Package type) {
      this.d = t.getDownloader();
      this.type = type;

      try {
         this.url = new URL("http://u.to/tlauncher-update/ixhQBA");
      } catch (MalformedURLException var4) {
         throw new TLauncherException("Cannot create update link!", var4);
      }
   }

   public void addListener(UpdaterListener l) {
      this.listeners.add(l);
   }

   public void removeListener(UpdaterListener l) {
      this.listeners.remove(l);
   }

   public void asyncFindUpdate() {
      if (this.enabled) {
         AsyncThread.execute(new Runnable() {
            public void run() {
               Updater.this.findUpdate();
            }
         });
      }

   }

   public void findUpdate() {
      try {
         this.findUpdate_();
      } catch (Exception var2) {
         this.log("Error on searching for update", var2);
      }

   }

   private void findUpdate_() throws IOException {
      if (!this.enabled) {
         this.log("Updater is not enabled");
      } else {
         this.log("Searching for an update...");
         if (this.type == Updater.Package.EXE) {
            File oldfile = new File(Wrapper.getExecutable().getAbsolutePath() + ".replace");
            if (oldfile.delete()) {
               this.log("Old version has been deleted (.replace)");
            }
         }

         this.onUpdaterRequests();
         this.update_download = new Downloadable(this.url);
         HttpURLConnection connection = this.update_download.makeConnection();
         int code = connection.getResponseCode();
         switch(code) {
         case 200:
            InputStream is = connection.getInputStream();
            this.update_settings = new Settings(is);
            connection.disconnect();
            this.found_version = this.update_settings.getDouble("last-version");
            if (this.found_version <= 0.0D) {
               throw new IllegalStateException("Settings file is invalid!");
            } else {
               if (0.1699D >= this.found_version) {
                  if (0.1699D > this.found_version) {
                     this.log("Running version is newer than found (" + this.found_version + ")");
                  }

                  this.noUpdateFound();
                  return;
               }

               String current_link = this.update_settings.get(this.type.toLowerCase());
               this.found_link = current_link;
               this.onUpdateFound();
               return;
            }
         default:
            throw new IllegalStateException("Response code (" + code + ") is not supported by Updater!");
         }
      }
   }

   public void downloadUpdate() {
      try {
         this.downloadUpdate_();
      } catch (Exception var2) {
         this.onUpdateError(var2);
      }

   }

   private void downloadUpdate_() throws MalformedURLException {
      this.log("Downloading update...");
      this.onUpdaterDownloads();
      this.launcher_destination = new File(MinecraftUtil.getWorkingDirectory(), "tlauncher.updated");
      this.launcher_destination.deleteOnExit();
      this.launcher_download = new Downloadable(this.found_link, this.launcher_destination);
      this.launcher_download.setHandler(new DownloadableHandler() {
         public void onStart() {
         }

         public void onCompleteError() {
            Updater.this.onUpdateError(Updater.this.launcher_download.getError());
         }

         public void onComplete() {
            Updater.this.onUpdateDownloaded();
         }
      });
      this.d.add(this.launcher_download);
      this.d.launch();
   }

   public String getLink() {
      return this.found_link;
   }

   public void saveUpdate() {
      try {
         this.saveUpdate_();
      } catch (Exception var2) {
         this.onProcessError(var2);
      }

   }

   private void saveUpdate_() throws Exception {
      this.log("Saving update... Launcher will be closed.");
      if (this.type == Updater.Package.EXE) {
         File oldfile = new File(this.replace.toString());
         File newfile = new File(this.replace.toString() + ".replace");
         if (!oldfile.renameTo(newfile)) {
            throw new IllegalStateException("Cannot rename " + oldfile + " to " + newfile);
         }
      }

      FileInputStream in = new FileInputStream(this.launcher_destination);
      FileOutputStream out = new FileOutputStream(this.replace);
      byte[] buffer = new byte[65536];

      for(int curread = in.read(buffer); curread > 0; curread = in.read(buffer)) {
         out.write(buffer, 0, curread);
      }

      in.close();
      out.close();
      System.exit(0);
   }

   public double getFoundVersion() {
      return this.found_version;
   }

   public URI getFoundLinkAsURI() {
      try {
         return new URI(this.found_link);
      } catch (URISyntaxException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   private void onUpdaterRequests() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterRequesting(this);
      }

   }

   private void noUpdateFound() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterNotFoundUpdate(this);
      }

   }

   private void onUpdateFound() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterFoundUpdate(this);
      }

   }

   private void onUpdaterDownloads() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterDownloading(this);
      }

   }

   private void onUpdateDownloaded() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterDownloadSuccess(this);
      }

   }

   private void onUpdateError(Throwable e) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         UpdaterListener l = (UpdaterListener)var3.next();
         l.onUpdaterDownloadError(this, e);
      }

   }

   private void onProcessError(Throwable e) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         UpdaterListener l = (UpdaterListener)var3.next();
         l.onUpdaterProcessError(this, e);
      }

   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   private void log(Object... obj) {
      Object[] var5 = obj;
      int var4 = obj.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         Object cobj = var5[var3];
         U.log("[Updater]", cobj);
      }

   }

   public static enum Package {
      EXE,
      JAR;

      public String toLowerCase() {
         return this.name().toLowerCase();
      }
   }
}
