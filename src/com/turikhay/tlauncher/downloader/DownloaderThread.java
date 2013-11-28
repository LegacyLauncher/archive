package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.util.FileUtil;
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
   public final int maxAttempts = 3;
   private final Downloader fd;
   private boolean launched;
   private boolean available = true;
   private boolean list_ = true;
   private List list;
   private List queue;
   private int done;
   private int remain;
   private int progress;
   private int av = 500;
   private int avi;
   private double each;
   private double speed;
   private double[] av_speed;
   private Throwable error;

   public DownloaderThread(Downloader td, int tid) {
      Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
      this.list = new ArrayList();
      this.queue = new ArrayList();
      this.av_speed = new double[this.av];
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
      Iterator var3 = this.list.iterator();

      while(var3.hasNext()) {
         Downloadable d = (Downloadable)var3.next();
         this.error = null;
         this.onStart(d);
         int attempt = 0;
         int max = d.getFast() ? 2 : 3;

         while(attempt < max) {
            ++attempt;
            this.dlog("Attempting to download " + d.getURL() + " [" + attempt + "/" + max + "]...");

            try {
               this.download(d, attempt, max);
               break;
            } catch (DownloaderError var7) {
               if (var7.isSerious()) {
                  this.log(d, var7);
                  this.onError(d, var7);
                  break;
               }

               if (var7.hasTimeout()) {
                  this.sleepFor((long)var7.getTimeout());
               }
            } catch (SocketTimeoutException var8) {
               this.log(d, "Timeout exception. Retrying.");
               this.sleepFor(5000L);
            } catch (Exception var9) {
               this.log(d, "Unknown error occurred.", var9);
               this.onError(d, var9);
               break;
            }
         }

         if (attempt >= max) {
            this.log(d, "Gave up trying to download this file [" + attempt + "/" + max + "]");
            this.onError(d, new DownloaderError("Gave up trying to download this file", true));
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

   public void download(Downloadable d, int attempt, int max) throws IOException {
      HttpURLConnection connection = d.makeConnection();
      int code = -1;
      long reply_s = System.currentTimeMillis();
      if (d.getFast()) {
         connection.connect();
      } else {
         code = connection.getResponseCode();
      }

      long reply = System.currentTimeMillis() - reply_s;
      this.dlog("Got reply in " + reply + " ms.");
      switch(code) {
      case 301:
      case 302:
      case 303:
      case 304:
         this.log(d, "File is not modified (304)");
         this.onComplete(d);
         return;
      case 307:
         String newurl = connection.getHeaderField("Location");
         connection.disconnect();
         if (newurl == null) {
            throw new DownloaderError("Redirection is required but field \"Location\" is empty", true);
         }

         this.dlog("Responce code is " + code + ". Redirecting to: " + newurl);
         d.setURL(newurl);
         this.download(d, 1, max);
         return;
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
      default:
         if (code < 200 || code > 299) {
            throw new DownloaderError("Illegal response code (" + code + ")", true);
         }
      case -1:
         InputStream in = connection.getInputStream();
         File file = d.getDestination();
         FileUtil.createFile(file);
         OutputStream out = new FileOutputStream(file);
         long read = 0L;
         long length = (long)connection.getContentLength();
         long downloaded_s = System.currentTimeMillis();
         byte[] buffer = new byte[65536];
         int curread = in.read(buffer);

         long downloaded_e;
         double curdone;
         while(curread > 0) {
            read += (long)curread;
            out.write(buffer, 0, curread);
            curread = in.read(buffer);
            if (curread == -1) {
               break;
            }

            downloaded_e = System.currentTimeMillis() - downloaded_s;
            if (downloaded_e >= 500L) {
               curdone = (double)((float)read / (float)length);
               double curspeed = (double)read / (double)downloaded_e;
               this.onProgress(curread, curdone, curspeed);
            }
         }

         downloaded_e = System.currentTimeMillis() - downloaded_s;
         curdone = downloaded_e != 0L ? (double)read / (double)downloaded_e : 0.0D;
         d.setTime(downloaded_e);
         d.setSize(read);
         in.close();
         out.close();
         connection.disconnect();
         File[] copies = d.getAdditionalDestinations();
         if (copies != null && copies.length > 0) {
            this.dlog("Found additional destinations. Copying...");
            File[] var29 = copies;
            int var28 = copies.length;

            for(int var27 = 0; var27 < var28; ++var27) {
               File copy = var29[var27];
               this.dlog("Copying " + copy + "...");
               FileUtil.copyFile(file, copy, d.isForced());
               this.dlog(d, "Success!");
            }

            this.dlog("Copying completed.");
         }

         this.log(d, "Downloaded in " + d.getTime() + " ms. at " + curdone + " kb/s [" + attempt + "/" + max + ";" + d.getFast() + "]");
         this.onComplete(d);
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
      return this.speed;
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
      this.fd.onProgress(this.id, this.progress, this.speed);
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

   private void onProgress(int curread, double curdone, double curspeed) {
      if (this.avi == this.av) {
         this.avi = 0;
      }

      this.av_speed[this.avi] = curspeed;
      int old_progress = this.progress;
      this.progress = (int)((double)this.done * this.each + curdone * this.each);
      this.speed = U.getAverage(this.av_speed);
      if (this.progress != old_progress) {
         this.fd.onProgress(this.id, this.progress, curspeed);
      }
   }

   private void dlog(Object... message) {
      String prefix = "[" + this.name + "DT #" + this.id + "]";
      U.log(prefix, message);
   }

   private void log(Downloadable d, Object... message) {
      DownloadableContainer c = d.getContainer();
      String prefix = "[" + this.name + "DT #" + this.id + "]\n> " + d.getURL() + "\n> ";
      if (c != null && c.hasConsole()) {
         c.log(prefix, message);
      }

      U.log(prefix, message);
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
