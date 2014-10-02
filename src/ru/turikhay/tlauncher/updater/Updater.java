package ru.turikhay.tlauncher.updater;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.SimpleConfiguration;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.exceptions.TLauncherException;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public class Updater {
   private static final String[] links = TLauncher.getUpdateRepos();
   private static final URI[] URIs = makeURIs();
   private final Downloader d;
   private final List listeners = Collections.synchronizedList(new ArrayList());
   private Update found;
   private SimpleConfiguration parsed;
   private Updater.UpdaterState state;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$updater$PackageType;

   public void addListener(UpdaterListener l) {
      this.listeners.add(l);
   }

   public void removeListener(UpdaterListener l) {
      this.listeners.remove(l);
   }

   public Updater(TLauncher t) {
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

   public Updater.UpdaterState getState() {
      return this.state;
   }

   Updater.UpdaterState findUpdate() {
      try {
         return this.state = this.findUpdate_();
      } catch (Throwable var2) {
         this.state = Updater.UpdaterState.ERROR;
         return this.state;
      }
   }

   private Updater.UpdaterState findUpdate_() {
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
            URL url = uri.toURL();
            HttpURLConnection connection = Downloadable.setUp(url.openConnection(), true);
            connection.setInstanceFollowRedirects(true);
            int code = connection.getResponseCode();
            switch(code) {
            case 200:
               InputStream is = connection.getInputStream();
               this.parsed = new SimpleConfiguration(is);
               connection.disconnect();
               Update update = new Update(this, this.d, this.parsed);
               double version = update.getVersion();
               log("Success! Found:", version);
               AdParser ad = AdParser.parseFrom(this.parsed);
               if (ad != null) {
                  this.onAdFound(ad);
               }

               if (TLauncher.getVersion() > version) {
                  log("Found version is older than running:", version, "(" + TLauncher.getVersion() + ")");
               }

               if (update.getDownloadLink() == null) {
                  log("An update for current package type is not available.");
               } else if (TLauncher.getVersion() < version) {
                  log("Found actual version:", version);
                  this.found = update;
                  this.onUpdateFound(update);
                  return Updater.UpdaterState.FOUND;
               }

               this.noUpdateFound();
               return Updater.UpdaterState.NOT_FOUND;
            default:
               throw new IllegalStateException("Response code (" + code + ") is not supported by Updater!");
            }
         } catch (Exception var14) {
            log("Cannot get update information", var14);
            ++var3;
         }
      }

      log("Updating is impossible - cannot get any information.");
      this.onUpdaterRequestError();
      return Updater.UpdaterState.ERROR;
   }

   public void notifyAboutUpdate() {
      if (this.found != null) {
         this.onUpdateFound(this.found);
      }
   }

   public Update getUpdate() {
      return this.found;
   }

   public SimpleConfiguration getParsed() {
      return this.parsed;
   }

   public void asyncFindUpdate() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            Updater.this.findUpdate();
         }
      });
   }

   private void onUpdaterRequests() {
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdaterListener l = (UpdaterListener)var3.next();
            l.onUpdaterRequesting(this);
         }

      }
   }

   private void onUpdaterRequestError() {
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdaterListener l = (UpdaterListener)var3.next();
            l.onUpdaterRequestError(this);
         }

      }
   }

   private void onUpdateFound(Update u) {
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdaterListener l = (UpdaterListener)var4.next();
            l.onUpdateFound(u);
         }

      }
   }

   private void noUpdateFound() {
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdaterListener l = (UpdaterListener)var3.next();
            l.onUpdaterNotFoundUpdate(this);
         }

      }
   }

   private void onAdFound(AdParser ad) {
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdaterListener l = (UpdaterListener)var4.next();
            l.onAdFound(this, ad);
         }

      }
   }

   private static boolean isAutomodeFor(PackageType pt) {
      if (pt == null) {
         throw new NullPointerException("PackageType is NULL!");
      } else {
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$updater$PackageType()[pt.ordinal()]) {
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
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$updater$PackageType()[pt.ordinal()]) {
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

   private static File getTempFileFor(PackageType pt) {
      return new File(getFileFor(pt).getAbsolutePath() + ".replace");
   }

   private static File getTempFile() {
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
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$updater$PackageType() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$updater$PackageType;
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

         $SWITCH_TABLE$ru$turikhay$tlauncher$updater$PackageType = var0;
         return var0;
      }
   }

   public static enum UpdaterState {
      READY,
      FOUND,
      NOT_FOUND,
      ERROR;
   }
}
