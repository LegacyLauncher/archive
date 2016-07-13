package ru.turikhay.tlauncher.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class Stats {
   private static final URL STATS_BASE = Http.constantURL("http://u.tlauncher.ru/stats/");
   private static final ExecutorService service = Executors.newCachedThreadPool();
   private static boolean allow = false;
   private static String lastResult;
   private static final List listeners = Collections.synchronizedList(new ArrayList());

   public static void setAllowed(boolean allowed) {
      allow = allowed;
   }

   public static void minecraftLaunched(Account account, CompleteVersion version, ServerList.Server server) {
      Stats.Args args = newAction("mc_launched").add("mc_version", version.getID()).add("account_type", account.getType().toString());
      if (server != null) {
         args.add("server", server.getAddress());
      }

      submitDenunciation(args);
   }

   public static void noticeViewed(Notices.Notice notice) {
      if (notice.getId() != 0) {
         submitDenunciation(newAction("notice_viewed").add("notice_id", String.valueOf(notice.getId())));
      }

   }

   private static Stats.Args newAction(String name) {
      return (new Stats.Args()).add("client", TLauncher.getInstance().getSettings().getClient().toString()).add("version", String.valueOf(TLauncher.getVersion())).add("brand", TLauncher.getBrand()).add("os", OS.CURRENT.getName()).add("locale", TLauncher.getInstance().getLang().getSelected().toString()).add("action", name);
   }

   private static void submitDenunciation(final Stats.Args args) {
      if (allow) {
         service.submit(new Callable() {
            public Void call() throws Exception {
               String result = Stats.performGetRequest(Stats.STATS_BASE, Stats.toRequest(args));
               if (StringUtils.isNotEmpty(result)) {
                  Stats.lastResult = result;
                  Iterator var2 = Stats.listeners.iterator();

                  while(var2.hasNext()) {
                     Stats.StatsListener l = (Stats.StatsListener)var2.next();
                     l.onInvalidSubmit(result);
                  }
               }

               return null;
            }
         });
      }

   }

   private static String toRequest(Stats.Args args) {
      StringBuilder b = new StringBuilder();
      Iterator var3 = args.map.entrySet().iterator();

      while(var3.hasNext()) {
         Entry arg = (Entry)var3.next();
         b.append('&').append(Http.encode((String)arg.getKey())).append('=').append(Http.encode((String)arg.getValue()));
      }

      return b.substring(1);
   }

   private static HttpURLConnection createUrlConnection(URL url) throws IOException {
      Validate.notNull(url);
      debug("Opening connection to " + url);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection(U.getProxy());
      connection.setConnectTimeout(U.getConnectionTimeout());
      connection.setReadTimeout(U.getReadTimeout());
      connection.setUseCaches(false);
      return connection;
   }

   public static String performGetRequest(URL url, String request) throws IOException {
      Validate.notNull(url);
      Validate.notNull(request);
      url = new URL(url.toString() + '?' + request);
      HttpURLConnection connection = createUrlConnection(url);
      debug("Reading data from " + url);
      InputStream inputStream = null;

      String var7;
      try {
         String result;
         try {
            inputStream = connection.getInputStream();
            result = IOUtils.toString(inputStream, Charsets.UTF_8);
            debug("Successful read, server response was " + connection.getResponseCode());
            debug("Response: " + result);
            String var6 = result;
            return var6;
         } catch (IOException var10) {
            IOUtils.closeQuietly(inputStream);
            inputStream = connection.getErrorStream();
            if (inputStream == null) {
               debug("Request failed", var10);
               throw var10;
            }

            debug("Reading error page from " + url);
            result = IOUtils.toString(inputStream, Charsets.UTF_8);
            debug("Successful read, server response was " + connection.getResponseCode());
            debug("Response: " + result);
            var7 = result;
         }
      } finally {
         IOUtils.closeQuietly(inputStream);
      }

      return var7;
   }

   private static void debug(Object... o) {
      if (TLauncher.getDebug()) {
         U.log("[Stats]", o);
      }

   }

   public static void addListener(Stats.StatsListener listener) {
      listeners.add(listener);
      if (lastResult != null) {
         listener.onInvalidSubmit(lastResult);
      }

   }

   public interface StatsListener {
      void onInvalidSubmit(String var1);
   }

   private static class Args {
      private final LinkedHashMap map;

      private Args() {
         this.map = new LinkedHashMap();
      }

      public Stats.Args add(String key, String value) {
         if (this.map.containsKey(key)) {
            this.map.remove(key);
         }

         this.map.put(key, value);
         return this;
      }

      // $FF: synthetic method
      Args(Object x0) {
         this();
      }
   }
}
