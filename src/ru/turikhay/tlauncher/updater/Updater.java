package ru.turikhay.tlauncher.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
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
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public class Updater {
   private final Gson gson = this.buildGson();
   private Update update;
   private final List listeners = Collections.synchronizedList(new ArrayList());

   public Update getUpdate() {
      return this.update;
   }

   protected Updater.SearchResult findUpdate0() {
      this.log("Requesting an update...");
      List errorList = new ArrayList();
      Updater.SearchResult result = null;
      Iterator var4 = this.getUpdateUrlList().iterator();

      while(var4.hasNext()) {
         String updateUrl = (String)var4.next();
         long startTime = System.currentTimeMillis();
         this.log("Requesting from:", updateUrl);

         try {
            URL url = new URL(updateUrl);
            HttpURLConnection connection = Downloadable.setUp(url.openConnection(U.getProxy()), true);
            if (connection.getResponseCode() != 200) {
               throw new IOException("illegal response code: " + connection.getResponseCode());
            }

            result = new Updater.SearchSucceeded((Updater.UpdaterResponse)this.gson.fromJson((Reader)(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"))), (Class)Updater.UpdaterResponse.class));
         } catch (Exception var9) {
            this.log("Failed to request from:", updateUrl, var9);
            result = null;
            errorList.add(var9);
         }

         this.log("Request time:", System.currentTimeMillis() - startTime, "ms");
         if (result != null) {
            this.log("Successfully requested from:", updateUrl);
            this.log(result);
            break;
         }
      }

      return (Updater.SearchResult)(result == null ? new Updater.SearchFailed(errorList) : result);
   }

   public Updater.SearchResult findUpdate() {
      Updater.SearchResult result = this.findUpdate0();
      this.dispatchResult(result);
      return result;
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

   protected void dispatchResult(Updater.SearchResult result) {
      requireNotNull(result, "result");
      UpdaterListener l;
      Iterator var4;
      if (result instanceof Updater.SearchSucceeded) {
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
      return (new GsonBuilder()).registerTypeAdapter(Ads.class, new Ads.Deserializer()).registerTypeAdapter(Update.class, new Update.Deserializer()).create();
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
      private Ads ads;

      public final Update getUpdate() {
         return this.update;
      }

      public final Ads getAds() {
         return this.ads;
      }

      public String toString() {
         return "UpdaterResponse{update=" + this.update + ", ads=" + this.ads + "}";
      }
   }
}
