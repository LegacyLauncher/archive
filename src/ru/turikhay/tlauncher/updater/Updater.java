package ru.turikhay.tlauncher.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher.Http;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public class Updater {
   private final Gson gson = this.buildGson();
   private boolean refreshed;
   private Update update;
   private final List listeners = Collections.synchronizedList(new ArrayList());

   public boolean getRefreshed() {
      return this.refreshed;
   }

   public void setRefreshed(boolean refreshed) {
      this.refreshed = refreshed;
   }

   public Update getUpdate() {
      return this.update;
   }

   private Updater.SearchResult localTestUpdate() {
      URL url = this.getClass().getResource("default.json");
      if (url == null) {
         return null;
      } else {
         InputStreamReader reader = null;

         try {
            reader = new InputStreamReader(url.openStream());
            Updater.SearchSucceeded response = new Updater.SearchSucceeded((Updater.UpdaterResponse)this.gson.fromJson((Reader)reader, (Class)Updater.UpdaterResponse.class));
            return response;
         } catch (Exception var8) {
         } finally {
            U.close(reader);
         }

         return null;
      }
   }

   protected Updater.SearchResult findUpdate0() {
      Updater.SearchResult result = null;
      if (TLauncher.getDebug()) {
         result = this.localTestUpdate();
         if (result != null) {
            this.log("Requested update from local file");
            return (Updater.SearchResult)result;
         }
      }

      this.log("Requesting an update...");
      List errorList = new ArrayList();
      String get = "?version=" + Http.encode(String.valueOf(TLauncher.getVersion())) + "&brand=" + Http.encode(TLauncher.getBrand()) + "&client=" + Http.encode(TLauncher.getInstance().getSettings().getClient().toString()) + "&beta=" + Http.encode(String.valueOf(TLauncher.isBeta()));
      Iterator var5 = this.getUpdateUrlList().iterator();

      while(var5.hasNext()) {
         String updateUrl = (String)var5.next();
         long startTime = System.currentTimeMillis();
         this.log("Requesting from:", updateUrl);
         String response = null;

         try {
            URL url = new URL(updateUrl + get);
            HttpURLConnection connection = Downloadable.setUp(url.openConnection(U.getProxy()), true);
            connection.setDoOutput(true);
            response = IOUtils.toString((Reader)(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"))));
            result = new Updater.SearchSucceeded((Updater.UpdaterResponse)this.gson.fromJson(response, Updater.UpdaterResponse.class));
         } catch (Exception var11) {
            this.log("Failed to request from:", updateUrl, var11);
            result = null;
            errorList.add(var11);
         }

         this.log("Request time:", System.currentTimeMillis() - startTime, "ms");
         if (result != null) {
            this.log("Successed!");
            break;
         }
      }

      if (this.refreshed) {
         return null;
      } else {
         return (Updater.SearchResult)(result == null ? new Updater.SearchFailed(errorList) : result);
      }
   }

   public Updater.SearchResult findUpdate() {
      try {
         Updater.SearchResult result = this.findUpdate0();
         this.dispatchResult(result);
         return result;
      } catch (Exception var2) {
         return null;
      }
   }

   public void asyncFindUpdate() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            Updater.this.findUpdate();
         }
      });
   }

   public void addListener(UpdaterListener l) {
      this.listeners.add(l);
   }

   public void removeListener(UpdaterListener l) {
      this.listeners.remove(l);
   }

   public void dispatchResult(Updater.SearchResult result) {
      requireNotNull(result, "result");
      UpdaterListener l;
      Iterator var4;
      if (result instanceof Updater.SearchSucceeded) {
         Stats.setAllowed(result.getResponse().getStatsAllowed());
         synchronized(this.listeners) {
            var4 = this.listeners.iterator();

            while(var4.hasNext()) {
               l = (UpdaterListener)var4.next();
               l.onUpdaterSucceeded((Updater.SearchSucceeded)result);
            }
         }
      } else {
         if (!(result instanceof Updater.SearchFailed)) {
            throw new IllegalArgumentException("unknown result of " + result.getClass());
         }

         synchronized(this.listeners) {
            var4 = this.listeners.iterator();

            while(var4.hasNext()) {
               l = (UpdaterListener)var4.next();
               l.onUpdaterErrored((Updater.SearchFailed)result);
            }
         }
      }

   }

   protected void onUpdaterRequests() {
      synchronized(this.listeners) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            UpdaterListener l = (UpdaterListener)var3.next();
            l.onUpdaterRequesting(this);
         }

      }
   }

   protected List getUpdateUrlList() {
      return Arrays.asList(TLauncher.getUpdateRepos());
   }

   protected Gson buildGson() {
      return (new GsonBuilder()).registerTypeAdapter(Notices.class, new Notices.Deserializer()).registerTypeAdapter(Update.class, new Update.Deserializer()).create();
   }

   public Updater.SearchSucceeded newSucceeded(Updater.UpdaterResponse response) {
      return new Updater.SearchSucceeded(response);
   }

   protected void log(Object... o) {
      U.log("[Updater]", o);
   }

   private static Object requireNotNull(Object obj, String name) {
      if (obj == null) {
         throw new NullPointerException(name);
      } else {
         return obj;
      }
   }

   public class SearchFailed extends Updater.SearchResult {
      protected final List errorList = new ArrayList();

      public SearchFailed(List list) {
         super((Updater.UpdaterResponse)null);
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            Throwable t = (Throwable)var4.next();
            if (t == null) {
               throw new NullPointerException();
            }
         }

         this.errorList.addAll(list);
      }

      public final List getCauseList() {
         return this.errorList;
      }

      public String toString() {
         return this.getClass().getSimpleName() + "{errors=" + this.errorList + "}";
      }
   }

   public abstract class SearchResult {
      protected final Updater.UpdaterResponse response;

      public SearchResult(Updater.UpdaterResponse response) {
         this.response = response;
      }

      public final Updater.UpdaterResponse getResponse() {
         return this.response;
      }

      public final Updater getUpdater() {
         return Updater.this;
      }

      public String toString() {
         return this.getClass().getSimpleName() + "{response=" + this.response + "}";
      }
   }

   public class SearchSucceeded extends Updater.SearchResult {
      public SearchSucceeded(Updater.UpdaterResponse response) {
         super((Updater.UpdaterResponse)Updater.requireNotNull(response, "response"));
      }
   }

   public static class UpdaterResponse {
      private Update update;
      private Notices ads;
      private boolean allowStats;

      public UpdaterResponse(Update update) {
         this.update = update;
      }

      public final Update getUpdate() {
         return this.update;
      }

      public final Notices getNotices() {
         return this.ads;
      }

      public final boolean getStatsAllowed() {
         return this.allowStats;
      }

      public String toString() {
         return "UpdaterResponse{update=" + this.update + ", notices=" + this.ads + "}";
      }
   }
}
