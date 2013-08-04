package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Downloadable {
   private URL url;
   private File destination;
   private String md5;
   private String filename;
   private Throwable error;
   private DownloadableContainer container;
   private DownloadableHandler handler;
   private boolean forced;

   public Downloadable(URL url, File destination, boolean force) {
      this.url = url;
      this.destination = destination;
      this.forced = force;
   }

   public Downloadable(String url, File destination, boolean force) throws MalformedURLException {
      this.url = new URL(url);
      this.destination = destination;
      this.forced = force;
   }

   public Downloadable(URL url, File destination) {
      this(url, destination, false);
   }

   public Downloadable(String url, File destination) throws MalformedURLException {
      this(url, destination, false);
   }

   public URL getURL() {
      return this.url;
   }

   public File getDestination() {
      return this.destination;
   }

   public String getMD5() {
      return this.md5 == null ? (this.md5 = FileUtil.getMD5Checksum(this.destination)) : this.md5;
   }

   public String getFilename() {
      return this.filename == null ? (this.filename = FileUtil.getFilename(this.url)) : this.filename;
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

   HttpURLConnection makeConnection() throws IOException {
      HttpURLConnection connection = (HttpURLConnection)this.url.openConnection();
      connection.setUseCaches(false);
      connection.setDefaultUseCaches(false);
      connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
      connection.setRequestProperty("Expires", "0");
      connection.setRequestProperty("Pragma", "no-cache");
      if (this.getMD5() != null) {
         connection.setRequestProperty("If-None-Match", this.md5);
      }

      connection.connect();
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
