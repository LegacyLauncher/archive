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
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.Bootstrapper;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableHandler;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class Update {
   protected double version;
   protected double requiredAtLeastFor;
   protected Map description = new HashMap();
   protected Map links = new HashMap();
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

   public Update(double version, double requiredAtLeastFor, Map description, Map links) {
      this.state = Update.State.NONE;
      this.downloader = this.getDefaultDownloader();
      this.listeners = Collections.synchronizedList(new ArrayList());
      this.version = version;
      this.requiredAtLeastFor = requiredAtLeastFor;
      if (description != null) {
         this.description.putAll(description);
         if (description.containsKey("ru_RU") && !description.containsKey("uk_UA")) {
            this.description.put("uk_UA", description.get("ru_RU"));
         }
      }

      if (links != null) {
         this.links.putAll(links);
      }

   }

   public double getVersion() {
      return this.version;
   }

   public void setVersion(double version) {
      this.version = version;
   }

   public double getRequiredAtLeastFor() {
      return this.requiredAtLeastFor;
   }

   public void setRequiredAtLeastFor(double version) {
      this.requiredAtLeastFor = version;
   }

   public Map getDescriptionMap() {
      return this.description;
   }

   public Map getLinks() {
      return this.links;
   }

   public String getLink(PackageType packageType) {
      return (String)this.links.get(packageType);
   }

   public String getLink() {
      return this.getLink(PackageType.CURRENT);
   }

   public Update.State getState() {
      return this.state;
   }

   protected void setState(Update.State newState) {
      if (newState.ordinal() <= this.state.ordinal() && this.state.ordinal() != Update.State.values().length - 1) {
         throw new IllegalStateException("tried to change from " + this.state + " to " + newState);
      } else {
         this.state = newState;
         this.log("Set state:", newState);
      }
   }

   public Downloader getDownloader() {
      return this.downloader;
   }

   public void setDownloader(Downloader downloader) {
      this.downloader = downloader;
   }

   public boolean isApplicable() {
      return StringUtils.isNotBlank((CharSequence)this.links.get(PackageType.CURRENT)) && TLauncher.getVersion() < this.version;
   }

   public boolean isRequired() {
      return this.requiredAtLeastFor != 0.0D && TLauncher.getVersion() <= this.requiredAtLeastFor;
   }

   public String getDescription(String key) {
      return this.description == null ? null : (String)this.description.get(key);
   }

   public String getDescription() {
      return this.getDescription(TLauncher.getInstance().getSettings().getLocale().toString());
   }

   protected void download0(PackageType packageType, boolean async) throws Throwable {
      this.setState(Update.State.DOWNLOADING);
      URL url = new URL(this.getLink(packageType));
      this.log("url:", url);
      File destination = new File(FileUtil.getRunningJar().getAbsolutePath() + ".update");
      destination.deleteOnExit();
      this.log("dest", destination);
      this.download = new Downloadable(url.toExternalForm(), destination);
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

   public void download() {
      this.download(false);
   }

   public void asyncDownload() {
      this.download(true);
   }

   protected void apply0() throws Throwable {
      this.setState(Update.State.APPLYING);
      File replace = FileUtil.getRunningJar();
      File replaceWith = this.download.getDestination();
      String[] args = TLauncher.getInstance() != null ? TLauncher.getArgs() : new String[0];
      ProcessBuilder builder = Bootstrapper.createLauncher(args).createProcess();
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

   public void removeListener(UpdateListener l) {
      this.listeners.remove(l);
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
      return "Update{version=" + this.version + "," + "requiredAtLeastFor=" + this.requiredAtLeastFor + "," + "description=" + this.description + "," + "links=" + this.links + "}";
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
            update.description.putAll(description);
         }

         Map links = (Map)context.deserialize(object.get("links"), (new TypeToken() {
         }).getType());
         if (links != null) {
            update.links.putAll(links);
         }

         return update;
      }
   }
}
