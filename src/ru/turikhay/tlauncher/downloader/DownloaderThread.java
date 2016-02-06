package ru.turikhay.tlauncher.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public class DownloaderThread extends ExtendedThread {
   private final int ID;
   private final String LOGGER_PREFIX;
   private final Downloader downloader;
   private final List list;
   private double currentProgress;
   private double lastProgress;
   private double doneProgress;
   private double eachProgress;
   private double speed;
   private Downloadable current;
   private boolean launched;
   private final StringBuilder b = new StringBuilder();
   private final Formatter formatter;
   double curdone;

   DownloaderThread(Downloader d, int id) {
      super("DT#" + id);
      this.formatter = new Formatter(this.b, Locale.US);
      this.ID = id;
      this.LOGGER_PREFIX = "[D#" + id + "]";
      this.downloader = d;
      this.list = new ArrayList();
      this.startAndWait();
   }

   int getID() {
      return this.ID;
   }

   void add(Downloadable d) {
      this.list.add(d);
   }

   void startDownload() {
      this.launched = true;
      this.unlockThread("iteration");
   }

   void stopDownload() {
      this.launched = false;
   }

   public void run() {
      while(true) {
         this.launched = true;
         this.eachProgress = 1.0D / (double)this.list.size();
         this.currentProgress = this.doneProgress = 0.0D;
         Iterator var2 = this.list.iterator();

         label55:
         while(var2.hasNext()) {
            Downloadable d = (Downloadable)var2.next();
            this.current = d;
            this.onStart();
            int attempt = 0;
            Object error = null;

            int max;
            Iterator var8;
            while(attempt < (max = this.downloader.getConfiguration().getTries(d.isFast()))) {
               ++attempt;
               this.dlog("Attempting to download (repo: " + d.getRepository() + ") [" + attempt + "/" + max + "]...");
               int timeout = attempt * this.downloader.getConfiguration().getTimeout();

               try {
                  this.download(timeout);
                  break;
               } catch (GaveUpDownloadException var10) {
                  this.dlog("File is not reachable at all.");
                  error = var10;
                  if (attempt >= max) {
                     FileUtil.deleteFile(d.getDestination());
                     var8 = d.getAdditionalDestinations().iterator();

                     while(var8.hasNext()) {
                        File downloadable = (File)var8.next();
                        FileUtil.deleteFile(downloadable);
                     }

                     this.dlog("Gave up trying to download this file.", var10);
                     this.onError(var10);
                  }
               } catch (AbortedDownloadException var11) {
                  this.dlog("This download process has been aborted.");
                  error = var11;
                  break;
               }
            }

            if (error instanceof AbortedDownloadException) {
               this.tlog("Thread is aborting...");
               var8 = this.list.iterator();

               while(true) {
                  if (!var8.hasNext()) {
                     break label55;
                  }

                  Downloadable var11 = (Downloadable)var8.next();
                  var11.onAbort((AbortedDownloadException)error);
               }
            }
         }

         this.speed = 0.0D;
         this.list.clear();
         this.lockThread("iteration");
         this.launched = false;
      }
   }

   private void download(int timeout) throws GaveUpDownloadException, AbortedDownloadException {
      boolean hasRepo = this.current.hasRepository();
      int attempt = 0;
      int max = hasRepo ? this.current.getRepository().getCount() : 1;
      Object cause = null;

      while(attempt < max) {
         ++attempt;
         String url = hasRepo ? this.current.getRepository().getSelectedRepo() + this.current.getURL() : this.current.getURL();
         this.dlog("Downloading: " + url);

         try {
            this.downloadURL(url, timeout);
            return;
         } catch (IOException var8) {
            this.dlog("Failed: " + url, var8);
            cause = var8;
            if (hasRepo) {
               this.current.getRepository().selectNext();
            }
         } catch (AbortedDownloadException var9) {
            throw var9;
         } catch (Throwable var10) {
            this.dlog("Unknown error occurred:", var10);
            cause = var10;
         }
      }

      throw new GaveUpDownloadException(this.current, (Throwable)cause);
   }

   private void downloadURL(String path, int timeout) throws IOException, AbortedDownloadException {
      URL url = new URL(path);
      URLConnection urlConnection = url.openConnection(U.getProxy());
      if (!(urlConnection instanceof HttpURLConnection)) {
         throw new IOException("Invalid protocol: " + url.getProtocol());
      } else {
         HttpURLConnection connection = (HttpURLConnection)urlConnection;
         Downloadable.setUp(connection, timeout, this.current.getInsertUA());
         if (!this.launched) {
            throw new AbortedDownloadException();
         } else {
            long reply_s = System.currentTimeMillis();
            connection.connect();
            long reply = System.currentTimeMillis() - reply_s;
            this.dlog("Replied in " + reply + " ms.");
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            File file = this.current.getDestination();
            File temp = FileUtil.makeTemp(new File(file.getAbsolutePath() + ".tlauncherdownload"));
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
            long read = 0L;
            long length = (long)connection.getContentLength();
            long downloaded_s = System.currentTimeMillis();
            long speed_s = downloaded_s;
            long timer = downloaded_s;
            byte[] buffer = new byte[65536];
            int curread = in.read(buffer);

            long downloaded_e;
            double downloadSpeed;
            while(curread > 0) {
               if (!this.launched) {
                  out.close();
                  throw new AbortedDownloadException();
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
                  downloadSpeed = length > 0L ? (double)((float)read / (float)length) : 0.0D;
                  double copies = downloaded_e > 0L ? (double)read / (double)downloaded_e : 0.0D;
                  if (speed_s - timer > 15000L) {
                     timer = speed_s;
                     this.b.setLength(0);
                     this.formatter.format("Still downloading: %.0f%% at speed %.1f kb/s", downloadSpeed * 100.0D, copies);
                     this.dlog(this.b.toString());
                  }

                  this.onProgress(downloadSpeed, copies);
               }
            }

            downloaded_e = System.currentTimeMillis() - downloaded_s;
            downloadSpeed = downloaded_e != 0L ? (double)read / (double)downloaded_e : 0.0D;
            in.close();
            out.close();
            connection.disconnect();
            FileUtil.copyFile(temp, file, true);
            FileUtil.deleteFile(temp);
            List copies1 = this.current.getAdditionalDestinations();
            if (copies1.size() > 0) {
               this.dlog("Found additional destinations. Copying...");
               Iterator var34 = copies1.iterator();

               while(var34.hasNext()) {
                  File copy = (File)var34.next();
                  this.dlog("Copying " + copy + "...");
                  FileUtil.copyFile(file, copy, this.current.isForce());
                  this.dlog("Success!");
               }

               this.dlog("Copying completed.");
            }

            this.dlog("Downloaded " + read / 1024L + " kb in " + downloaded_e + " ms. at " + downloadSpeed + " kb/s");
            this.onComplete();
         }
      }
   }

   private void onStart() {
      this.current.onStart();
   }

   private void onError(Throwable e) {
      this.current.onError(e);
      this.downloader.onFileComplete(this, this.current);
   }

   private void onProgress(double curdone, double curspeed) {
      this.curdone = curdone;
      this.currentProgress = this.doneProgress + this.eachProgress * curdone;
      this.speed = 0.005D * this.speed + 0.995D * curspeed;
      this.lastProgress = this.currentProgress;
      this.downloader.onProgress(this, this.currentProgress, curdone, this.speed);
   }

   private void onComplete() throws RetryDownloadException {
      this.doneProgress += this.eachProgress;
      this.current.onComplete();
      this.downloader.onProgress(this, this.doneProgress, 1.0D, this.speed);
      this.downloader.onFileComplete(this, this.current);
   }

   private void tlog(Object... o) {
      U.plog(this.LOGGER_PREFIX, o);
   }

   private void dlog(Object... o) {
      U.plog(this.LOGGER_PREFIX, "> " + this.current.getURL() + "\n ", o);
      if (this.current.hasConsole()) {
         this.current.getContainer().getConsole().log("> " + this.current.getURL() + "\n  ", o);
      }

   }
}
