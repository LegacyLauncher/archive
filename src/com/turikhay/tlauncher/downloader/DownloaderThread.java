package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DownloaderThread extends Thread {
   public final String name;
   public final int id;
   public final int maxAttempts = 10;
   private final Downloader fd;
   private boolean launched;
   private boolean available = true;
   private boolean list_ = true;
   private List list = new ArrayList();
   private List queue = new ArrayList();
   private int done;
   private int remain;
   private int progress;
   private double each;
   private double av_speed;
   private Throwable error;

   public DownloaderThread(Downloader td, int tid) {
      this.fd = td;
      this.name = this.fd.name;
      this.id = tid;
      this.start();
   }

   public void run() {
      this.check();

      while(!this.launched) {
         this.sleepFor(500L);
      }

      this.available = false;
      this.done = this.progress = 0;
      int all = this.list.size();
      this.remain = all;
      this.each = 100.0D / (double)all;
      this.log("Received " + all + " files");
      Iterator var3 = this.list.iterator();

      while(var3.hasNext()) {
         Downloadable d = (Downloadable)var3.next();
         this.error = null;
         this.onStart(d);
         int attempt = 0;

         while(attempt < 10) {
            ++attempt;
            this.log("Attempting to download " + d.getURL() + " [" + attempt + "/" + 10 + "]...");

            try {
               this.download(d);
               this.log("Downloaded! [" + attempt + "/" + 10 + "]");
               break;
            } catch (DownloaderError var6) {
               if (var6.isSerious()) {
                  var6.printStackTrace();
                  this.onError(d, var6);
                  break;
               }

               if (var6.hasTimeout()) {
                  this.sleepFor((long)var6.getTimeout());
               }
            } catch (SocketTimeoutException var7) {
               this.log("Timeout exception. Retrying.");
               this.sleepFor(5000L);
            } catch (Exception var8) {
               this.log("Unknown error occurred.");
               var8.printStackTrace();
               this.onError(d, var8);
               break;
            }
         }
      }

      while(!this.list_) {
         this.sleepFor(100L);
      }

      this.list_ = false;
      this.list.clear();
      this.list.addAll(this.queue);
      this.queue.clear();
      this.list_ = true;
      this.available = true;
      this.launched = this.list.size() > 0;
      this.run();
   }

   public void download(Downloadable d) throws IOException {
      String fn = d.getFilename();
      long reply_s = System.currentTimeMillis();
      HttpURLConnection connection = d.makeConnection();
      long reply = System.currentTimeMillis() - reply_s;
      this.log("Got reply in " + reply + " ms.");
      int code = connection.getResponseCode();
      switch(code) {
      case 301:
      case 302:
      case 303:
      case 307:
         String newurl = connection.getHeaderField("Location");
         connection.disconnect();
         if (newurl == null) {
            throw new DownloaderError("Redirection is required but field \"Location\" is empty", true);
         }

         this.log("Responce code is " + code + ". Redirecting to: " + newurl);
         d.setURL(newurl);
         this.download(d);
         return;
      case 304:
         if (!d.isForced()) {
            this.log("File is not modified (304)");
            this.onComplete(d);
            return;
         }
         break;
      case 403:
         throw new DownloaderError("Forbidden (403)", true);
      case 404:
         throw new DownloaderError("Not Found (404)", true);
      case 408:
         throw new DownloaderError("Request Timeout (408)", false);
      case 500:
         throw new DownloaderError("Internal Server Error (500)", 5000);
      case 502:
         throw new DownloaderError("Bad Gateway (502)", 5000);
      case 503:
         throw new DownloaderError("Service Unavailable (503)", 5000);
      case 504:
         throw new DownloaderError("Gateway Timeout (504)", true);
      }

      if (code >= 200 && code <= 299) {
         File file = d.getDestination();
         if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
         }

         InputStream in = connection.getInputStream();
         OutputStream out = new FileOutputStream(file);
         long read = 0L;
         long length = connection.getContentLengthLong();
         long downloaded_s = System.currentTimeMillis();
         byte[] buffer = new byte[65536];
         int curread = in.read(buffer);

         long downloaded;
         while(curread > 0) {
            read += (long)curread;
            out.write(buffer, 0, curread);
            downloaded = System.nanoTime();
            curread = in.read(buffer);
            long curelapsed = System.nanoTime() - downloaded;
            double curdone = (double)((float)read / (float)length);
            this.onProgress(curread, curelapsed, curdone);
         }

         downloaded = System.currentTimeMillis() - downloaded_s;
         in.close();
         out.close();
         connection.disconnect();
         this.log("Successfully downloaded " + fn + " in " + downloaded / 1000L + " s!");
         this.onComplete(d);
      } else {
         throw new DownloaderError("Illegal response code (" + code + ")", true);
      }
   }

   public void add(Downloadable d) {
      while(!this.list_) {
         this.sleepFor(100L);
      }

      this.list_ = false;
      if (this.available) {
         this.list.add(d);
      } else {
         this.queue.add(d);
      }

      this.list_ = true;
   }

   public boolean isAvailable() {
      return this.available;
   }

   public boolean isLaunched() {
      return this.launched;
   }

   public Throwable getError() {
      return this.error;
   }

   public double getSpeed() {
      return this.av_speed;
   }

   public int getProgress() {
      return this.progress;
   }

   public int getRemain() {
      return this.remain;
   }

   public int getDone() {
      return this.done;
   }

   public void launch() {
      this.launched = true;
   }

   private void onStart(Downloadable d) {
      d.onStart();
      if (d.hasContainter()) {
         d.getContainer().onStart();
      }

      this.fd.onStart(this.id, d);
   }

   private void onComplete(Downloadable d) {
      d.onComplete();
      if (d.hasContainter()) {
         d.getContainer().onFileComplete();
      }

      --this.remain;
      ++this.done;
      this.progress = (int)((double)this.done * this.each);
      this.fd.onProgress(this.id, this.progress);
      this.fd.onComplete(this.id, d);
   }

   private void onError(Downloadable d, Throwable err) {
      this.error = err;
      d.setError(err);
      d.onError();
      if (d.hasContainter()) {
         d.getContainer().onError();
      }

      this.fd.onError(this.id, d);
   }

   private void onProgress(int curread, long curelapsed, double curdone) {
      int old_progress = this.progress;
      this.progress = (int)((double)this.done * this.each + curdone * this.each);
      if (this.progress != old_progress) {
         this.fd.onProgress(this.id, this.progress);
      }
   }

   private void log(Object message) {
      U.log("[" + this.name + "DT #" + this.id + "] ", message);
   }

   private void check() {
      if (!this.available) {
         throw new IllegalStateException("DownloaderThread (#" + this.id + ") is unavailable!");
      }
   }

   private void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var4) {
      }

   }
}
