package ru.turikhay.tlauncher.downloader;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.U;

public class Downloadable {
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

   protected Downloadable() {
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

   public boolean isFast() {
      return this.fastDownload;
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
         connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
         connection.setRequestProperty("Pragma", "no-cache");
         connection.setRequestProperty("Expires", "0");
         return connection;
      }
   }

   public static HttpURLConnection setUp(URLConnection connection, boolean fake) {
      return setUp(connection, U.getConnectionTimeout(), fake);
   }
}
