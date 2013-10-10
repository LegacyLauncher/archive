package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import net.minecraft.launcher_.Http;

public class Downloadable {
   public static final int CONNECTION_TIMEOUT = 30000;
   public static final int READ_TIMEOUT = 10000;
   private URL url;
   private File destination;
   private File[] copies;
   private Throwable error;
   private DownloadableContainer container;
   private DownloadableHandler handler;
   private boolean forced;

   public Downloadable(String url, File destination, File[] copies, boolean force) throws MalformedURLException {
      this.url = new URL(Http.encode(url));
      this.destination = destination;
      this.forced = force;
   }

   public Downloadable(String url, File destination, boolean force) throws MalformedURLException {
      this(url, destination, new File[0], force);
   }

   public Downloadable(URL url, boolean force) {
      this.url = url;
   }

   public Downloadable(String url, File destination) throws MalformedURLException {
      this(url, destination, false);
   }

   public Downloadable(URL url) {
      this(url, false);
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

   public String getMD5() {
      return FileUtil.getMD5Checksum(this.destination);
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

   public void onStart() {
      if (this.handler != null) {
         this.handler.onStart();
      }

   }

   public void onComplete() {
      if (this.handler != null) {
         this.handler.onComplete();
      }

   }

   public void onError() {
      if (this.handler != null) {
         this.handler.onCompleteError();
      }

   }

   public void setHandler(DownloadableHandler newhandler) {
      this.handler = newhandler;
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

   public HttpURLConnection makeConnection() throws IOException {
      HttpURLConnection connection = (HttpURLConnection)this.url.openConnection();
      setUp(connection);
      if (!this.forced) {
         String md5 = this.getMD5();
         if (md5 != null) {
            connection.setRequestProperty("If-None-Match", md5);
         }
      }

      connection.connect();
      return connection;
   }

   public String toString() {
      String r = "{";
      r = r + "url=" + (this.url == null ? null : this.url.toExternalForm());
      r = r + ",destination=" + this.destination;
      r = r + ",additionaldestinations=" + U.toLog(this.copies);
      r = r + ",error=" + this.error;
      r = r + ",container=" + this.container;
      r = r + ",handler=" + this.handler;
      r = r + ",forced=" + this.forced;
      r = r + "}";
      return r;
   }

   public static URLConnection setUp(URLConnection connection) {
      connection.setConnectTimeout(30000);
      connection.setReadTimeout(10000);
      connection.setUseCaches(false);
      connection.setDefaultUseCaches(false);
      connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
      connection.setRequestProperty("Expires", "0");
      connection.setRequestProperty("Pragma", "no-cache");
      return connection;
   }

   public static String getEtag(String etag) {
      if (etag == null) {
         etag = "-";
      } else if (etag.startsWith("\"") && etag.endsWith("\"")) {
         etag = etag.substring(1, etag.length() - 1);
      }

      return etag;
   }
}
