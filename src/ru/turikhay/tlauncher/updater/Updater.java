package ru.turikhay.tlauncher.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import net.minecraft.launcher.Http;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.util.Compressor;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public class Updater {
   private final Gson gson = this.buildGson();
   private boolean refreshed;
   private final List listeners = Collections.synchronizedList(new ArrayList());

   public void setRefreshed(boolean refreshed) {
      this.refreshed = refreshed;
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
            Updater.SearchSucceeded var4 = response;
            return var4;
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
      ArrayList errorList = new ArrayList();
      String get = "?version=" + Http.encode(String.valueOf(TLauncher.getVersion())) + "&brand=" + Http.encode(TLauncher.getBrand()) + "&client=" + Http.encode(TLauncher.getInstance().getSettings().getClient().toString()) + "&beta=" + Http.encode(String.valueOf(TLauncher.isBeta()));
      Iterator var5 = this.getUpdateUrlList().iterator();
      int attempt = 0;

      while(var5.hasNext()) {
         ++attempt;
         String updateUrl = (String)var5.next();
         long startTime = System.currentTimeMillis();
         this.log("Requesting from:", updateUrl);

         try {
            URL e = new URL(updateUrl + get);
            HttpURLConnection connection = Downloadable.setUp(e.openConnection(U.getProxy()), true);
            connection.setConnectTimeout(3000 * attempt);
            connection.setReadTimeout(3000 * attempt);
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            InputStream in = this.uncompress(connection);
            result = new Updater.SearchSucceeded((Updater.UpdaterResponse)this.gson.fromJson((Reader)(new InputStreamReader(in, "UTF-8")), (Class)Updater.UpdaterResponse.class));

            try {
               Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
               long refreshTime = ((Updater.SearchResult)result).getResponse().getRefreshTime();
               long currentTimeGMT = calendar.getTimeInMillis() / 1000L;
               calendar.setTimeInMillis(refreshTime * 1000L);
               this.log("Next refresh time:", calendar.getTime());
               if (refreshTime > 0L && currentTimeGMT > refreshTime) {
                  throw new IOException("refresh time exceeded");
               }
            } catch (IOException var17) {
               throw var17;
            } catch (Exception var18) {
               this.log(var18);
            }
         } catch (Exception var19) {
            this.log("Failed to request from:", updateUrl, var19);
            result = null;
            errorList.add(var19);
         }

         this.log("Request time:", System.currentTimeMillis() - startTime, "ms");
         if (result != null) {
            this.log("Successed!");
            break;
         }
      }

      return (Updater.SearchResult)(this.refreshed ? null : (result == null ? new Updater.SearchFailed(errorList) : result));
   }

   public Updater.SearchResult findUpdate() {
      try {
         Updater.SearchResult e = this.findUpdate0();
         this.dispatchResult(e);
         return e;
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

   public void dispatchResult(Updater.SearchResult result) {
      requireNotNull(result, "result");
      List var2;
      UpdaterListener l;
      Iterator var4;
      if (result instanceof Updater.SearchSucceeded) {
         Stats.setAllowed(result.getResponse().getStatsAllowed());
         var2 = this.listeners;
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

         var2 = this.listeners;
         synchronized(this.listeners) {
            var4 = this.listeners.iterator();

            while(var4.hasNext()) {
               l = (UpdaterListener)var4.next();
               l.onUpdaterErrored((Updater.SearchFailed)result);
            }
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

   private InputStream uncompress(URLConnection connection) throws IOException {
      InputStream in = connection.getInputStream();
      Object in;
      if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
         this.log("Data is compressed by the server with gzip");
         in = new GzipCompressorInputStream(in);
      } else {
         in = Compressor.uncompressMarked(in);
         if (in instanceof Compressor.CompressedInputStream) {
            this.log("Data is compressed by the script with", ((Compressor.CompressedInputStream)in).getCompressor().getName());
         } else {
            this.log("Data is not compressed at all");
         }
      }

      return (InputStream)in;
   }

   public static class UpdaterResponse {
      private Update update;
      private Notices ads;
      private boolean allowStats;
      private long refreshTime;

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

      public final long getRefreshTime() {
         return this.refreshTime;
      }

      public String toString() {
         return "UpdaterResponse{update=" + this.update + ", notices=" + this.ads + "}";
      }
   }

   public class SearchSucceeded extends Updater.SearchResult {
      public SearchSucceeded(Updater.UpdaterResponse response) {
         super(response);
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

      public String toString() {
         return this.getClass().getSimpleName() + "{response=" + this.response + "}";
      }
   }

   public class SearchFailed extends Updater.SearchResult {
      protected final List errorList = new ArrayList();

      public SearchFailed(List list) {
         super((Updater.UpdaterResponse)null);
         Iterator var4 = list.iterator();

         Throwable t;
         do {
            if (!var4.hasNext()) {
               this.errorList.addAll(list);
               return;
            }

            t = (Throwable)var4.next();
         } while(t != null);

         throw new NullPointerException();
      }

      public String toString() {
         return this.getClass().getSimpleName() + "{errors=" + this.errorList + "}";
      }
   }
}
