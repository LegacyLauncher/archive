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
import ru.turikhay.tlauncher.exceptions.IOExceptionList;
import ru.turikhay.tlauncher.repository.IRepo;
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

                     this.dlog("Gave up trying to download this file.");
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
      List exL = new ArrayList();
      Throwable cause = null;
      List list;
      if (this.current.hasRepository()) {
         list = this.current.getRepository().getRelevant().getList();
         int attempt = 0;

         for(int max = list.size(); attempt < max; ++attempt) {
            cause = null;
            Iterator var7 = list.iterator();

            while(var7.hasNext()) {
               IRepo repo = (IRepo)var7.next();
               URLConnection connection = null;

               try {
                  connection = repo.get(this.current.getURL(), attempt * this.downloader.getConfiguration().getTimeout(), U.getProxy());
                  this.dlog("Downloading:", connection);
                  this.downloadURL(connection, timeout);
                  return;
               } catch (IOException var11) {
                  this.dlog("Failed:", connection.getURL(), this.current.getURL(), var11.getMessage());
                  this.current.getRepository().getList().markInvalid(repo);
                  exL.add(var11);
               } catch (AbortedDownloadException var12) {
                  throw var12;
               } catch (Throwable var13) {
                  this.dlog("Unknown error occurred:", var13);
                  cause = var13;
               }
            }
         }
      } else {
         list = null;

         try {
            URLConnection connection = (new URL(this.current.getURL())).openConnection();
            this.dlog("Downloading:", connection);
            this.downloadURL(connection, timeout);
            return;
         } catch (IOException var14) {
            this.dlog("Failed:", list.getURL(), this.current.getURL(), var14.getMessage());
            exL.add(var14);
         } catch (AbortedDownloadException var15) {
            throw var15;
         } catch (Throwable var16) {
            this.dlog("Unknown error occurred:", var16);
            cause = var16;
         }
      }

      throw new GaveUpDownloadException(this.current, (Throwable)(cause == null ? new IOExceptionList(exL) : cause));
   }

   private void downloadURL(URLConnection urlConnection, int timeout) throws IOException, AbortedDownloadException {
      if (!(urlConnection instanceof HttpURLConnection)) {
         throw new IOException("invalid protocol");
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
            File temp = new File(file.getAbsoluteFile() + ".download");
            if (temp.isFile()) {
               FileUtil.deleteFile(temp);
            } else {
               FileUtil.createFile(temp);
            }

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

            this.dlog("Downloaded " + read / 1024L + " kb in " + downloaded_e + " ms. at " + U.setFractional(downloadSpeed, 2) + " kb/s");
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
      if (this.current.hasLogger()) {
         this.current.getContainer().getLogger().log("> " + this.current.getURL() + "\n  ", o);
      }

   }
}
