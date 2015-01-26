package ru.turikhay.tlauncher.downloader;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import ru.turikhay.tlauncher.handlers.SimpleHostnameVerifier;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class Downloadable {
   private static final boolean DEFAULT_FORCE = false;
   private static final boolean DEFAULT_FAST = false;
   private String path;
   private Repository repo;
   private File destination;
   private final List additionalDestinations;
   private boolean forceDownload;
   private boolean fastDownload;
   private boolean insertUseragent;
   private boolean locked;
   private DownloadableContainer container;
   private final List handlers;
   private Throwable error;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$util$OS;

   private Downloadable() {
      this.additionalDestinations = Collections.synchronizedList(new ArrayList());
      this.handlers = Collections.synchronizedList(new ArrayList());
   }

   public Downloadable(Repository repo, String path, File destination, boolean forceDownload, boolean fastDownload) {
      this();
      this.setURL(repo, path);
      this.setDestination(destination);
      this.forceDownload = forceDownload;
      this.fastDownload = fastDownload;
   }

   public Downloadable(Repository repo, String path, File destination, boolean forceDownload) {
      this(repo, path, destination, forceDownload, false);
   }

   public Downloadable(Repository repo, String path, File destination) {
      this(repo, path, destination, false, false);
   }

   private Downloadable(String url, File destination, boolean forceDownload, boolean fastDownload) {
      this();
      this.setURL(url);
      this.setDestination(destination);
      this.forceDownload = forceDownload;
      this.fastDownload = fastDownload;
   }

   public Downloadable(String url, File destination) {
      this(url, destination, false, false);
   }

   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (this == o) {
         return true;
      } else if (!(o instanceof Downloadable)) {
         return false;
      } else {
         Downloadable c = (Downloadable)o;
         return U.equal(this.path, c.path) && U.equal(this.repo, c.repo) && U.equal(this.destination, c.destination) && U.equal(this.additionalDestinations, c.additionalDestinations);
      }
   }

   public boolean getInsertUA() {
      return this.insertUseragent;
   }

   public void setInsertUA(boolean ua) {
      this.checkLocked();
      this.insertUseragent = ua;
   }

   public boolean isForce() {
      return this.forceDownload;
   }

   public void setForce(boolean force) {
      this.checkLocked();
      this.forceDownload = force;
   }

   public boolean isFast() {
      return this.fastDownload;
   }

   public void setFast(boolean fast) {
      this.checkLocked();
      this.fastDownload = fast;
   }

   public String getURL() {
      return this.path;
   }

   public Repository getRepository() {
      return this.repo;
   }

   public boolean hasRepository() {
      return this.repo != null;
   }

   protected void setURL(Repository repo, String path) {
      if (repo == null) {
         throw new NullPointerException("Repository is NULL!");
      } else if (path == null) {
         throw new NullPointerException("Path is NULL!");
      } else {
         this.checkLocked();
         this.repo = repo;
         this.path = path;
      }
   }

   protected void setURL(String url) {
      if (url == null) {
         throw new NullPointerException();
      } else if (url.isEmpty()) {
         throw new IllegalArgumentException("URL cannot be empty!");
      } else {
         this.checkLocked();
         this.repo = null;
         this.path = url;
      }
   }

   public File getDestination() {
      return this.destination;
   }

   public String getFilename() {
      return FileUtil.getFilename(this.path);
   }

   protected void setDestination(File file) {
      if (file == null) {
         throw new NullPointerException();
      } else {
         this.checkLocked();
         this.destination = file;
      }
   }

   public List getAdditionalDestinations() {
      return Collections.unmodifiableList(this.additionalDestinations);
   }

   public void addAdditionalDestination(File file) {
      if (file == null) {
         throw new NullPointerException();
      } else {
         this.checkLocked();
         this.additionalDestinations.add(file);
      }
   }

   public DownloadableContainer getContainer() {
      return this.container;
   }

   public boolean hasContainer() {
      return this.container != null;
   }

   public boolean hasConsole() {
      return this.container != null && this.container.hasConsole();
   }

   public void addHandler(DownloadableHandler handler) {
      if (handler == null) {
         throw new NullPointerException();
      } else {
         this.checkLocked();
         this.handlers.add(handler);
      }
   }

   protected void setContainer(DownloadableContainer container) {
      this.checkLocked();
      this.container = container;
   }

   public Throwable getError() {
      return this.error;
   }

   private void setLocked(boolean locked) {
      this.locked = locked;
   }

   protected void checkLocked() {
      if (this.locked) {
         throw new IllegalStateException("Downloadable is locked!");
      }
   }

   protected void onStart() {
      this.setLocked(true);
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler handler = (DownloadableHandler)var2.next();
         handler.onStart(this);
      }

   }

   protected void onAbort(AbortedDownloadException ae) {
      this.setLocked(false);
      this.error = ae;
      Iterator var3 = this.handlers.iterator();

      while(var3.hasNext()) {
         DownloadableHandler handler = (DownloadableHandler)var3.next();
         handler.onAbort(this);
      }

      if (this.container != null) {
         this.container.onAbort(this);
      }

   }

   protected void onComplete() throws RetryDownloadException {
      this.setLocked(false);
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler handler = (DownloadableHandler)var2.next();
         handler.onComplete(this);
      }

      if (this.container != null) {
         this.container.onComplete(this);
      }

   }

   protected void onError(Throwable e) {
      this.error = e;
      if (e != null) {
         this.setLocked(false);
         Iterator var3 = this.handlers.iterator();

         while(var3.hasNext()) {
            DownloadableHandler handler = (DownloadableHandler)var3.next();
            handler.onError(this, e);
         }

         if (this.container != null) {
            this.container.onError(this, e);
         }

      }
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{path='" + this.path + "'; " + "repo=" + this.repo + "; " + "destinations=" + this.destination + "," + this.additionalDestinations + "; " + "force=" + this.forceDownload + "; " + "fast=" + this.fastDownload + "; " + "locked=" + this.locked + "; " + "container=" + this.container + "; " + "handlers=" + this.handlers + "; " + "error=" + this.error + ";" + "}";
   }

   public static HttpURLConnection setUp(URLConnection connection0, int timeout, boolean fake) {
      if (connection0 == null) {
         throw new NullPointerException();
      } else if (!(connection0 instanceof HttpURLConnection)) {
         throw new IllegalArgumentException("Unknown connection protocol: " + connection0);
      } else {
         HttpURLConnection connection = (HttpURLConnection)connection0;
         connection.setConnectTimeout(timeout);
         connection.setReadTimeout(timeout);
         connection.setUseCaches(false);
         connection.setDefaultUseCaches(false);
         connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
         connection.setRequestProperty("Expires", "0");
         connection.setRequestProperty("Pragma", "no-cache");
         HttpsURLConnection securedConnection = (HttpsURLConnection)Reflect.cast(connection, HttpsURLConnection.class);
         if (securedConnection != null) {
            securedConnection.setHostnameVerifier(SimpleHostnameVerifier.getInstance());
         }

         if (!fake) {
            return connection;
         } else {
            String userAgent;
            switch($SWITCH_TABLE$ru$turikhay$util$OS()[OS.CURRENT.ordinal()]) {
            case 2:
               userAgent = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0; .NET4.0C)";
               break;
            case 3:
               userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8) AppleWebKit/535.18.5 (KHTML, like Gecko) Version/5.2 Safari/535.18.5";
               break;
            default:
               userAgent = "Mozilla/5.0 (Linux; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0";
            }

            connection.setRequestProperty("User-Agent", userAgent);
            return connection;
         }
      }
   }

   public static HttpURLConnection setUp(URLConnection connection, int timeout) {
      return setUp(connection, timeout, false);
   }

   public static HttpURLConnection setUp(URLConnection connection, boolean fake) {
      return setUp(connection, U.getConnectionTimeout(), fake);
   }

   public static HttpURLConnection setUp(URLConnection connection) {
      return setUp(connection, false);
   }

   public static String getEtag(String etag) {
      if (etag == null) {
         return "-";
      } else {
         return etag.startsWith("\"") && etag.endsWith("\"") ? etag.substring(1, etag.length() - 1) : etag;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$util$OS() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$util$OS;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[OS.values().length];

         try {
            var0[OS.LINUX.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[OS.OSX.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[OS.SOLARIS.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[OS.UNKNOWN.ordinal()] = 5;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[OS.WINDOWS.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$util$OS = var0;
         return var0;
      }
   }
}
