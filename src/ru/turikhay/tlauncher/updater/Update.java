package ru.turikhay.tlauncher.updater;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.Bootstrapper;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableHandler;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class Update {
   protected double version;
   protected double requiredAtLeastFor;
   protected Map description = new HashMap();
   protected Map downloads = new HashMap();
   @Expose(
      serialize = false,
      deserialize = false
   )
   protected Update.State state;
   @Expose(
      serialize = false,
      deserialize = false
   )
   protected Downloader downloader;
   @Expose(
      serialize = false,
      deserialize = false
   )
   private Downloadable download;
   @Expose(
      serialize = false,
      deserialize = false
   )
   private final List listeners;

   public Update() {
      this.state = Update.State.NONE;
      this.downloader = this.getDefaultDownloader();
      this.listeners = Collections.synchronizedList(new ArrayList());
   }

   public Update(double version, double requiredAtLeastFor, Map description, Map downloads) {
      this.state = Update.State.NONE;
      this.downloader = this.getDefaultDownloader();
      this.listeners = Collections.synchronizedList(new ArrayList());
      this.version = version;
      this.requiredAtLeastFor = requiredAtLeastFor;
      if (description != null) {
         this.description.putAll(description);
      }

      if (downloads != null) {
         this.downloads.putAll(downloads);
      }

   }

   public double getVersion() {
      return this.version;
   }

   public String getLink(PackageType packageType) {
      return (String)this.downloads.get(packageType);
   }

   public String getLink() {
      return this.getLink(PackageType.CURRENT);
   }

   protected void setState(Update.State newState) {
      if (newState.ordinal() <= this.state.ordinal() && this.state.ordinal() != Update.State.values().length - 1) {
         throw new IllegalStateException("tried to change from " + this.state + " to " + newState);
      } else {
         this.state = newState;
         this.log("Set state:", newState);
      }
   }

   public boolean isApplicable() {
      return StringUtils.isNotBlank((CharSequence)this.downloads.get(PackageType.CURRENT)) && TLauncher.getVersion() < this.version;
   }

   public boolean isRequired() {
      return TLauncher.isBeta() || this.requiredAtLeastFor != 0.0D && TLauncher.getVersion() <= this.requiredAtLeastFor;
   }

   public String getDescription(String key) {
      return this.description == null ? null : (String)this.description.get(key);
   }

   public String getDescription() {
      return this.getDescription(TLauncher.getInstance().getSettings().getLocale().toString());
   }

   protected void download0(PackageType packageType, boolean async) throws Throwable {
      this.setState(Update.State.DOWNLOADING);
      File destination = new File(FileUtil.getRunningJar().getAbsolutePath() + ".update");
      String link = this.getLink(packageType);
      if (link.startsWith("/")) {
         this.download = new Downloadable(Repository.EXTRA_VERSION_REPO, link.substring(1), destination);
      } else {
         this.download = new Downloadable((new URL(link)).toExternalForm(), destination);
      }

      this.download.setInsertUA(true);
      this.download.addHandler(new DownloadableHandler() {
         public void onStart(Downloadable d) {
         }

         public void onAbort(Downloadable d) {
            Update.this.onUpdateDownloadError(d.getError());
         }

         public void onComplete(Downloadable d) {
            Update.this.onUpdateReady();
         }

         public void onError(Downloadable d, Throwable e) {
            Update.this.onUpdateDownloadError(e);
         }
      });
      this.downloader.add(this.download);
      this.onUpdateDownloading();
      if (async) {
         this.downloader.startDownload();
      } else {
         this.downloader.startDownloadAndWait();
      }

   }

   public void download(PackageType type, boolean async) {
      try {
         this.download0(type, async);
      } catch (Throwable var4) {
         this.onUpdateError(var4);
      }

   }

   public void download(boolean async) {
      this.download(PackageType.CURRENT, async);
   }

   protected void apply0() throws Throwable {
      this.setState(Update.State.APPLYING);
      File replace = FileUtil.getRunningJar();
      File replaceWith = this.download.getDestination();
      String[] args = TLauncher.getInstance() != null ? TLauncher.getArgs() : new String[0];
      ProcessBuilder builder = Bootstrapper.createLauncher(args, false).createProcess();
      this.onUpdateApplying();
      FileInputStream in = new FileInputStream(replaceWith);
      FileOutputStream out = new FileOutputStream(replace);
      byte[] buffer = new byte[2048];

      for(int read = in.read(buffer); read > 0; read = in.read(buffer)) {
         out.write(buffer, 0, read);
      }

      try {
         in.close();
      } catch (IOException var11) {
      }

      try {
         out.close();
      } catch (IOException var10) {
      }

      try {
         builder.start();
      } catch (Throwable var9) {
      }

      System.exit(0);
   }

   public void apply() {
      try {
         this.apply0();
      } catch (Throwable var2) {
         this.onUpdateApplyError(var2);
      }

   }

   public void addListener(UpdateListener l) {
      this.listeners.add(l);
   }

   protected void onUpdateError(Throwable e) {
      this.setState(Update.State.ERRORED);
      List var2 = this.listeners;
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdateListener l = (UpdateListener)var4.next();
            l.onUpdateError(this, e);
         }

      }
   }

   protected void onUpdateDownloading() {
      List var1 = this.listeners;
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdateListener l = (UpdateListener)var3.next();
            l.onUpdateDownloading(this);
         }

      }
   }

   protected void onUpdateDownloadError(Throwable e) {
      this.setState(Update.State.ERRORED);
      List var2 = this.listeners;
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdateListener l = (UpdateListener)var4.next();
            l.onUpdateDownloadError(this, e);
         }

      }
   }

   protected void onUpdateReady() {
      this.setState(Update.State.READY);
      List var1 = this.listeners;
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdateListener l = (UpdateListener)var3.next();
            l.onUpdateReady(this);
         }

      }
   }

   protected void onUpdateApplying() {
      List var1 = this.listeners;
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdateListener l = (UpdateListener)var3.next();
            l.onUpdateApplying(this);
         }

      }
   }

   protected void onUpdateApplyError(Throwable e) {
      this.setState(Update.State.ERRORED);
      List var2 = this.listeners;
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            UpdateListener l = (UpdateListener)var4.next();
            l.onUpdateApplyError(this, e);
         }

      }
   }

   protected Downloader getDefaultDownloader() {
      return TLauncher.getInstance().getDownloader();
   }

   protected void log(Object... o) {
      U.log("[Update:" + this.version + "]", o);
   }

   public String toString() {
      return "Update{version=" + this.version + ",requiredAtLeastFor=" + this.requiredAtLeastFor + ",description=" + this.description + ",downloads=" + this.downloads + "}";
   }

   public static enum State {
      NONE,
      DOWNLOADING,
      READY,
      APPLYING,
      ERRORED;
   }

   public static class Deserializer implements JsonDeserializer {
      public Update deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
         try {
            return this.deserialize0(json, context);
         } catch (Exception var5) {
            U.log("Cannot parse update:", var5);
            return new Update();
         }
      }

      private Update deserialize0(JsonElement json, JsonDeserializationContext context) {
         JsonObject object = json.getAsJsonObject();
         Update update = new Update();
         update.version = object.get("version").getAsDouble();
         update.requiredAtLeastFor = object.has("requiredAtLeastFor") ? object.get("requiredAtLeastFor").getAsDouble() : 0.0D;
         Map description = (Map)context.deserialize(object.get("description"), (new TypeToken() {
         }).getType());
         if (description != null) {
            if (!TLauncher.getBrand().equals("Legacy") && description.containsKey("en_US")) {
               String universalDescription = (String)description.get("en_US");
               Locale[] var7 = TLauncher.getInstance().getLang().getLocales();
               int var8 = var7.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  Locale locale = var7[var9];
                  if (!description.containsKey(locale.toString())) {
                     description.put(locale.toString(), universalDescription);
                  }
               }
            } else if (description.containsKey("ru_RU") && !description.containsKey("uk_UA")) {
               description.put("uk_UA", description.get("ru_RU"));
            }

            update.description.putAll(description);
         }

         Map links = (Map)context.deserialize(object.get("downloads"), (new TypeToken() {
         }).getType());
         if (links != null) {
            update.downloads.putAll(links);
         }

         return update;
      }
   }
}
