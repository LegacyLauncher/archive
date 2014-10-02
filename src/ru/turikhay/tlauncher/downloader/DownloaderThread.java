package ru.turikhay.tlauncher.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public class DownloaderThread extends ExtendedThread {
   private static final String ITERATION_BLOCK = "iteration";
   private static final int CONTAINER_SIZE = 100;
   private static final int NOTIFY_TIMER = 15000;
   private final int ID;
   private final String LOGGER_PREFIX;
   private final Downloader downloader;
   private final List list;
   private final double[] averageSpeedContainer;
   private int speedCaret;
   private double currentProgress;
   private double lastProgress;
   private double doneProgress;
   private double eachProgress;
   private double speed;
   private Downloadable current;
   private boolean launched;

   DownloaderThread(Downloader d, int id) {
      super("DT#" + id);
      this.ID = id;
      this.LOGGER_PREFIX = "[D#" + id + "]";
      this.downloader = d;
      this.list = new ArrayList();
      this.averageSpeedContainer = new double[100];
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
      this.unblockThread(new String[]{"iteration"});
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

         label54:
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
               } catch (GaveUpDownloadException var9) {
                  this.dlog("File is not reachable at all.");
                  error = var9;
                  if (attempt >= max) {
                     FileUtil.deleteFile(d.getDestination());
                     var8 = d.getAdditionalDestinations().iterator();

                     while(var8.hasNext()) {
                        File file = (File)var8.next();
                        FileUtil.deleteFile(file);
                     }

                     this.dlog("Gave up trying to download this file.", var9);
                     this.onError(var9);
                  }
               } catch (AbortedDownloadException var10) {
                  this.dlog("This download process has been aborted.");
                  error = var10;
                  break;
               }
            }

            if (error instanceof AbortedDownloadException) {
               this.tlog("Thread is aborting...");
               var8 = this.list.iterator();

               while(true) {
                  if (!var8.hasNext()) {
                     break label54;
                  }

                  Downloadable downloadable = (Downloadable)var8.next();
                  downloadable.onAbort((AbortedDownloadException)error);
               }
            }
         }

         Arrays.fill(this.averageSpeedContainer, 0.0D);
         this.list.clear();
         this.blockThread("iteration");
         this.launched = false;
      }
   }

   private void download(int timeout) throws GaveUpDownloadException, AbortedDownloadException {
      boolean hasRepo = this.current.hasRepository();
      int attempt = 0;
      int max = hasRepo ? this.current.getRepository().getCount() : 1;
      IOException cause = null;

      while(attempt < max) {
         ++attempt;
         String url = hasRepo ? this.current.getRepository().getSelectedRepo() + this.current.getURL() : this.current.getURL();
         this.dlog("Trying to download from: " + url);

         try {
            this.downloadURL(url, timeout);
            return;
         } catch (IOException var8) {
            this.dlog("Failed to download from: " + url, var8);
            cause = var8;
            if (hasRepo) {
               this.current.getRepository().selectNext();
            }
         }
      }

      throw new GaveUpDownloadException(this.current, cause);
   }

   private void downloadURL(String path, int timeout) throws IOException, AbortedDownloadException, RetryDownloadException {
      URL url = new URL(path);
      URLConnection urlConnection = url.openConnection();
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
            this.dlog("Got reply in " + reply + " ms.");
            InputStream in = new BufferedInputStream(connection.getInputStream());
            File file = this.current.getDestination();
            File temp = FileUtil.makeTemp(new File(file.getAbsolutePath() + ".tlauncherdownload"));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
            long read = 0L;
            long length = (long)connection.getContentLength();
            long downloaded_s = System.currentTimeMillis();
            long speed_s = downloaded_s;
            long timer = downloaded_s;
            byte[] buffer = new byte[65536];
            int curread = in.read(buffer);

            long downloaded_e;
            double curdone;
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
                  curdone = (double)((float)read / (float)length);
                  double curspeed = (double)read / (double)downloaded_e;
                  if (speed_s - timer > 15000L) {
                     timer = speed_s;
                     this.dlog("Still downloading:", (int)(curdone * 100.0D) + "% at speed", U.setFractional(curspeed, 2), "kb/s");
                  }

                  this.onProgress((double)curread, curdone, curspeed);
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

            List copies = this.current.getAdditionalDestinations();
            if (copies.size() > 0) {
               this.dlog("Found additional destinations. Copying...");
               Iterator var34 = copies.iterator();

               while(var34.hasNext()) {
                  File copy = (File)var34.next();
                  this.dlog("Copying " + copy + "...");
                  FileUtil.copyFile(file, copy, this.current.isForce());
                  this.dlog("Success!");
               }

               this.dlog("Copying completed.");
            }

            this.dlog("Downloaded in " + downloaded_e + " ms. at " + curdone + " kb/s");
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

   private void onProgress(double curread, double curdone, double curspeed) {
      if (++this.speedCaret == 100) {
         this.speedCaret = 0;
      }

      this.averageSpeedContainer[this.speedCaret] = curspeed;
      this.currentProgress = this.doneProgress + this.eachProgress * curdone;
      if (!(this.currentProgress - this.lastProgress < 0.01D)) {
         this.lastProgress = this.currentProgress;
         this.speed = U.getAverage(this.averageSpeedContainer);
         this.downloader.onProgress(this, this.currentProgress, this.speed);
      }
   }

   private void onComplete() throws RetryDownloadException {
      this.doneProgress += this.eachProgress;
      this.downloader.onProgress(this, this.doneProgress, this.speed);
      this.downloader.onFileComplete(this, this.current);
      this.current.onComplete();
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
