package com.turikhay.tlauncher.downloader;

import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
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
   private final Downloader fd;
   private boolean launched;
   private boolean available = true;
   private boolean list_ = true;
   private List list = new ArrayList();
   private List queue = new ArrayList();
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
         int max = d.getFast() ? this.fd.getMinTries() : this.fd.getMaxTries();

         while(attempt < max) {
            ++attempt;
            this.dlog("Attempting to download " + d.getURL() + " [" + attempt + "/" + max + "]...");

            try {
               this.download(d, attempt, max);
               break;
            } catch (DownloaderThread.DownloaderStoppedException var7) {
               this.log(d, "Aborting...");
               if (d.getSize() > 0L) {
                  this.log(d, "Nevermind, file has been downloaded in time C:");
                  return;
               }

               if (!d.getDestination().delete()) {
                  this.log(d, "Cannot delete destination file, will be deleted on exit.");
                  d.getDestination().deleteOnExit();
               } else {
                  this.log(d, "Successfully deleted destination file!");
               }

               this.list.clear();
               this.queue.clear();
               this.available = true;
               this.launched = false;
               this.onAbort(d);
               this.run();
               break;
            } catch (DownloaderError var8) {
               if (var8.isSerious()) {
                  this.log(d, var8);
                  this.onError(d, var8);
                  break;
               }

               if (var8.hasTimeout()) {
                  this.sleepFor((long)var8.getTimeout());
               }
            } catch (SocketTimeoutException var9) {
               this.log(d, "Timeout exception. Retrying.");
               this.sleepFor(5000L);
            } catch (Exception var10) {
               this.log(d, "Unknown error occurred.", var10);
               this.onError(d, var10);
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
      File file = d.getDestination();

      try {
         file.canWrite();
      } catch (SecurityException var35) {
         throw new IOException("Cannot write into destination file!", var35);
      }

      HttpURLConnection connection = d.makeConnection();
      if (!this.launched) {
         throw new DownloaderThread.DownloaderStoppedException((DownloaderThread.DownloaderStoppedException)null);
      } else {
         int code = -1;
         long reply_s = System.currentTimeMillis();
         if (d.getFast()) {
            connection.connect();
         } else {
            code = connection.getResponseCode();
         }

         long reply = System.currentTimeMillis() - reply_s;
         if (!this.launched) {
            throw new DownloaderThread.DownloaderStoppedException((DownloaderThread.DownloaderStoppedException)null);
         } else {
            this.dlog("Got reply in " + reply + " ms. Response code:", code);
            switch(code) {
            case -1:
               break;
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
            }

            InputStream in = connection.getInputStream();
            File temp = FileUtil.makeTemp(new File(d.getDestination().getAbsolutePath() + ".tlauncherdownload"));
            OutputStream out = new FileOutputStream(temp);
            long read = 0L;
            long length = (long)connection.getContentLength();
            long downloaded_s = System.currentTimeMillis();
            long speed_s = downloaded_s;
            byte[] buffer = new byte[65536];
            int curread = in.read(buffer);

            long downloaded_e;
            double curdone;
            while(curread > 0) {
               if (!this.launched) {
                  out.close();
                  throw new DownloaderThread.DownloaderStoppedException((DownloaderThread.DownloaderStoppedException)null);
               }

               read += (long)curread;
               out.write(buffer, 0, curread);
               curread = in.read(buffer);
               if (curread == -1) {
                  break;
               }

               long speed_e = System.currentTimeMillis() - speed_s;
               if (speed_e >= 50L) {
                  speed_s = System.currentTimeMillis();
                  downloaded_e = speed_s - downloaded_s;
                  curdone = (double)((float)read / (float)length);
                  double curspeed = (double)read / (double)downloaded_e;
                  this.onProgress(curread, curdone, curspeed);
               }
            }

            downloaded_e = System.currentTimeMillis() - downloaded_s;
            curdone = downloaded_e != 0L ? (double)read / (double)downloaded_e : 0.0D;
            in.close();
            out.close();
            connection.disconnect();
            if (!temp.renameTo(file)) {
               FileUtil.copyFile(temp, file, true);
               FileUtil.deleteFile(temp);
            }

            File[] copies = d.getAdditionalDestinations();
            if (copies != null && copies.length > 0) {
               this.dlog("Found additional destinations. Copying...");
               File[] var34 = copies;
               int var33 = copies.length;

               for(int var32 = 0; var32 < var33; ++var32) {
                  File copy = var34[var32];
                  this.dlog("Copying " + copy + "...");
                  FileUtil.copyFile(file, copy, d.isForced());
                  this.dlog(d, "Success!");
               }

               this.dlog("Copying completed.");
            }

            d.setTime(downloaded_e);
            d.setSize(read);
            this.log(d, "Downloaded in " + d.getTime() + " ms. at " + curdone + " kb/s [" + attempt + "/" + max + ";" + d.getFast() + "]");
            if (!this.launched) {
               throw new DownloaderThread.DownloaderStoppedException((DownloaderThread.DownloaderStoppedException)null);
            } else {
               this.onComplete(d);
            }
         }
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

   public void startLaunch() {
      this.launched = true;
   }

   public void stopLaunch() {
      this.launched = false;
   }

   private void onStart(Downloadable d) {
      d.onStart();
      if (d.hasContainter()) {
         d.getContainer().onStart();
      }

      this.fd.onStart(this.id, d);
   }

   private void onAbort(Downloadable d) {
      d.onAbort();
      if (d.hasContainter()) {
         d.getContainer().onAbort();
      }

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
      ++this.avi;
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

   private class DownloaderStoppedException extends RuntimeException {
      private static final long serialVersionUID = 1383043531539603476L;

      private DownloaderStoppedException() {
      }

      // $FF: synthetic method
      DownloaderStoppedException(DownloaderThread.DownloaderStoppedException var2) {
         this();
      }
   }
}
