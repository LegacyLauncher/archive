package com.turikhay.tlauncher.updater;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.exceptions.TLauncherException;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Updater {
   public static final String[] links = new String[]{"http://u.to/tlauncher-original/BlPcBA", "http://ru-minecraft.org/update/original.ini", "http://dl.dropboxusercontent.com/u/6204017/update/original.ini"};
   public static final URI[] URIs = makeURIs();
   private final GlobalSettings s;
   private final Downloader d;
   private List listeners = new ArrayList();
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$updater$PackageType;

   public void addListener(UpdaterListener l) {
      this.listeners.add(l);
   }

   public void removeListener(UpdaterListener l) {
      this.listeners.remove(l);
   }

   public Updater(TLauncher t) {
      this.s = t.getSettings();
      this.d = t.getDownloader();
      if (!PackageType.isCurrent(PackageType.JAR)) {
         File oldfile = getTempFile();
         if (oldfile.delete()) {
            log("Old version has been deleted (.update)");
         }
      }

      log("Initialized.");
      log("Package type:", PackageType.getCurrent());
   }

   public void findUpdate() {
      log("Requesting an update...");
      this.onUpdaterRequests();
      int attempt = 0;
      URI[] var5;
      int var4 = (var5 = URIs).length;
      int var3 = 0;

      while(var3 < var4) {
         URI uri = var5[var3];
         ++attempt;
         log("Attempt #" + attempt + ". URL:", uri);

         try {
            Downloadable downloadable = new Downloadable(uri.toURL());
            HttpURLConnection connection = downloadable.makeConnection();
            int code = connection.getResponseCode();
            switch(code) {
            case 200:
               InputStream is = connection.getInputStream();
               Settings parsed = new Settings(is);
               connection.disconnect();
               Update update = new Update(this.d, parsed);
               double version = update.getVersion();
               log("Success!");
               if (0.1991D > version) {
                  log("Found version is older than running:", version);
               }

               if (update.getDownloadLink() == null) {
                  log("The update for current package type is not available.");
                  return;
               }

               if (!(0.1991D >= version)) {
                  log("Found actual version:", version);
                  this.onUpdateFound(update);
                  return;
               }

               Ad ad = new Ad(parsed);
               if (this.s.getInteger("updater.ad") != ad.getID() && ad.canBeShown()) {
                  this.onAdFound(ad);
               }

               this.noUpdateFound();
               return;
            default:
               throw new IllegalStateException("Response code (" + code + ") is not supported by Updater!");
            }
         } catch (Exception var15) {
            log("Cannot get update information", var15);
            ++var3;
         }
      }

      log("Updating is impossible - cannot get any information.");
      this.onUpdaterRequestError();
   }

   public void asyncFindUpdate() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            Updater.this.findUpdate();
         }
      });
   }

   private void onUpdaterRequests() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterRequesting(this);
      }

   }

   private void onUpdaterRequestError() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterRequestError(this);
      }

   }

   private void onUpdateFound(Update u) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         UpdaterListener l = (UpdaterListener)var3.next();
         l.onUpdateFound(this, u);
      }

   }

   private void noUpdateFound() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         UpdaterListener l = (UpdaterListener)var2.next();
         l.onUpdaterNotFoundUpdate(this);
      }

   }

   private void onAdFound(Ad ad) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         UpdaterListener l = (UpdaterListener)var3.next();
         l.onAdFound(this, ad);
      }

   }

   public static boolean isAutomodeFor(PackageType pt) {
      if (pt == null) {
         throw new NullPointerException("PackageType is NULL!");
      } else {
         switch($SWITCH_TABLE$com$turikhay$tlauncher$updater$PackageType()[pt.ordinal()]) {
         case 1:
         case 2:
            return true;
         default:
            throw new IllegalArgumentException("Unknown PackageType!");
         }
      }
   }

   public static boolean isAutomode() {
      return isAutomodeFor(PackageType.getCurrent());
   }

   public static File getFileFor(PackageType pt) {
      if (pt == null) {
         throw new NullPointerException("PackageType is NULL!");
      } else {
         switch($SWITCH_TABLE$com$turikhay$tlauncher$updater$PackageType()[pt.ordinal()]) {
         case 1:
         case 2:
            return FileUtil.getRunningJar();
         default:
            throw new IllegalArgumentException("Unknown PackageType!");
         }
      }
   }

   public static File getFile() {
      return getFileFor(PackageType.getCurrent());
   }

   public static File getUpdateFileFor(PackageType pt) {
      return new File(getFileFor(pt).getAbsolutePath() + ".update");
   }

   public static File getUpdateFile() {
      return getUpdateFileFor(PackageType.getCurrent());
   }

   public static File getTempFileFor(PackageType pt) {
      return new File(getFileFor(pt).getAbsolutePath() + ".replace");
   }

   public static File getTempFile() {
      return getTempFileFor(PackageType.getCurrent());
   }

   private static URI[] makeURIs() {
      int len = links.length;
      URI[] r = new URI[len];

      for(int i = 0; i < len; ++i) {
         try {
            r[i] = (new URL(links[i])).toURI();
         } catch (Exception var4) {
            throw new TLauncherException("Cannot create link from at i:" + i, var4);
         }
      }

      return r;
   }

   private static void log(Object... obj) {
      U.log("[Updater]", obj);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$updater$PackageType() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$updater$PackageType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[PackageType.values().length];

         try {
            var0[PackageType.EXE.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[PackageType.JAR.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$tlauncher$updater$PackageType = var0;
         return var0;
      }
   }
}
