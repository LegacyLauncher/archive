package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher.Http;

public class Downloadable {
   public static final String DEFAULT_CHECKSUM_ALGORITHM = "SHA-1";
   private List handlers;
   private URL url;
   private File destination;
   private File[] copies;
   private DownloadableContainer container;
   private boolean forced;
   private boolean fast;
   private long time;
   private long size;
   protected Throwable error;

   public Downloadable(URL url, File destination, File[] copies, boolean force) {
      this.handlers = new ArrayList();
      this.url = url;
      this.destination = destination;
      this.copies = copies;
      this.forced = force;
   }

   public Downloadable(String url, File destination, File[] copies, boolean force) throws MalformedURLException {
      this(new URL(Http.encode(url)), destination, copies, force);
   }

   public Downloadable(String url, File destination, boolean force) throws MalformedURLException {
      this(url, destination, new File[0], force);
   }

   public Downloadable(URL url, boolean force) {
      this.handlers = new ArrayList();
      this.url = url;
      this.forced = force;
   }

   public Downloadable(String url, File destination) throws MalformedURLException {
      this(url, destination, false);
   }

   public Downloadable(URL url) {
      this(url, false);
   }

   public Downloadable(URL url, File destination) {
      this((URL)url, destination, (File[])null, false);
   }

   public URL getURL() {
      return this.url;
   }

   public File getDestination() {
      return this.destination;
   }

   public File[] getAdditionalDestinations() {
      return this.copies;
   }

   public String getHash(String algorithm) {
      return FileUtil.getChecksum(this.destination, algorithm);
   }

   public String getFilename() {
      return Http.decode(FileUtil.getFilename(this.url));
   }

   public Throwable getError() {
      return this.error;
   }

   public boolean isForced() {
      return this.forced;
   }

   public boolean hasContainter() {
      return this.container != null;
   }

   public DownloadableContainer getContainer() {
      return this.container;
   }

   public long getTime() {
      return this.time;
   }

   public long getSize() {
      return this.size;
   }

   public boolean getFast() {
      return this.fast;
   }

   public void onStart() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler h = (DownloadableHandler)var2.next();
         h.onStart();
      }

   }

   public void onComplete() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler h = (DownloadableHandler)var2.next();
         h.onComplete();
      }

   }

   public void onError() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler h = (DownloadableHandler)var2.next();
         h.onCompleteError();
      }

   }

   public void onAbort() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         DownloadableHandler h = (DownloadableHandler)var2.next();
         h.onAbort();
      }

   }

   public void addHandler(DownloadableHandler newhandler) {
      this.handlers.add(newhandler);
   }

   void setError(Throwable e) {
      this.error = e;
   }

   void setContainer(DownloadableContainer newcontainer) {
      this.container = newcontainer;
   }

   public void setURL(URL newurl) {
      this.url = newurl;
   }

   public void setURL(String newurl) throws MalformedURLException {
      this.url = new URL(newurl);
   }

   public void setDestination(File newdestination) {
      this.destination = newdestination;
   }

   public void setAdditionalDestinations(File[] newdestinations) {
      this.copies = newdestinations;
   }

   public void setForced(boolean newforced) {
      this.forced = newforced;
   }

   public void setTime(long ms) {
      this.time = ms;
   }

   public void setSize(long b) {
      this.size = b;
   }

   public void setFast(boolean newfast) {
      this.fast = newfast;
   }

   public HttpURLConnection makeConnection() throws IOException {
      HttpURLConnection connection = (HttpURLConnection)this.url.openConnection();
      setUp(connection, false);
      String md5 = this.getHash("MD5");
      if (md5 != null) {
         connection.setRequestProperty("If-None-Match", md5);
      }

      return connection;
   }

   public String toString() {
      String r = "{";
      r = r + "url=" + (this.url == null ? null : this.url.toExternalForm());
      r = r + ",destination=" + this.destination;
      r = r + ",additionaldestinations=" + U.toLog(this.copies);
      r = r + ",error=" + this.error;
      r = r + ",container=" + this.container;
      r = r + ",handlers=" + U.toLog(this.handlers);
      r = r + ",forced=" + this.forced;
      r = r + "}";
      return r;
   }

   public static URLConnection setUp(URLConnection connection, boolean fake) {
      connection.setConnectTimeout(U.getConnectionTimeout());
      connection.setReadTimeout(U.getReadTimeout());
      connection.setUseCaches(false);
      connection.setDefaultUseCaches(false);
      connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
      connection.setRequestProperty("Expires", "0");
      connection.setRequestProperty("Pragma", "no-cache");
      if (!fake) {
         return connection;
      } else {
         connection.setRequestProperty("Accept", "text/html, application/xml;q=0.9, application/xhtml xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1");
         connection.setRequestProperty("Accept-Language", "en");
         connection.setRequestProperty("Accept-Charset", "iso-8859-1, utf-8, utf-16, *;q=0.1");
         connection.setRequestProperty("Accept-Encoding", "deflate, gzip, x-gzip, identity, *;q=0");
         connection.setRequestProperty("User-Agent", "Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.16");
         return connection;
      }
   }

   public static URLConnection setUp(URLConnection connection) {
      return setUp(connection, false);
   }

   public static String getEtag(String etag) {
      if (etag == null) {
         return "-";
      } else {
         return etag.startsWith("\"") && etag.endsWith("\"") ? etag.substring(1, etag.length() - 1) : etag;
      }
   }
}
