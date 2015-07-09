package ru.turikhay.tlauncher.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.util.U;

public class Stats {
   private static final URL STATS_BASE = Http.constantURL("http://tlauncher.ru/stats/");
   private static final ExecutorService service = Executors.newCachedThreadPool();
   private static boolean allow = false;

   public static boolean getAllowed() {
      return allow;
   }

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

   private static Stats.Args newAction(String name) {
      return (new Stats.Args((Stats.Args)null)).add("client", TLauncher.getInstance().getSettings().getClient().toString()).add("version", String.valueOf(TLauncher.getVersion())).add("action", name);
   }

   private static void submitDenunciation(final Stats.Args args) {
      if (allow) {
         service.submit(new Callable() {
            public Void call() throws Exception {
               Stats.performGetRequest(Stats.STATS_BASE, Stats.toRequest(args));
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
         try {
            inputStream = connection.getInputStream();
            String result = IOUtils.toString(inputStream, Charsets.UTF_8);
            debug("Successful read, server response was " + connection.getResponseCode());
            debug("Response: " + result);
            var7 = result;
            return var7;
         } catch (IOException var10) {
            IOUtils.closeQuietly(inputStream);
            inputStream = connection.getErrorStream();
            if (inputStream == null) {
               debug("Request failed", var10);
               throw var10;
            }
         }

         debug("Reading error page from " + url);
         String result = IOUtils.toString(inputStream, Charsets.UTF_8);
         debug("Successful read, server response was " + connection.getResponseCode());
         debug("Response: " + result);
         var7 = result;
      } finally {
         IOUtils.closeQuietly(inputStream);
      }

      return var7;
   }

   private static void debug(Object... o) {
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

      public Stats.Args remove(String key) {
         this.map.remove(key);
         return this;
      }

      // $FF: synthetic method
      Args(Stats.Args var1) {
         this();
      }
   }
}
