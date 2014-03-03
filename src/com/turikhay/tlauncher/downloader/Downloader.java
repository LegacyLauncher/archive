package com.turikhay.tlauncher.downloader;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.util.U;
import com.turikhay.util.async.ExtendedThread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Downloader extends ExtendedThread {
   public static final int MAX_THREADS = 8;
   static final String ITERATION_BLOCK = "iteration";
   static final String DOWNLOAD_BLOCK = "download";
   private final DownloaderThread[] threads;
   private final List list;
   private final List listeners;
   private Configuration.ConnectionQuality configuration;
   private final AtomicInteger remainingObjects;
   private int runningThreads;
   private final double[] speedContainer;
   private final double[] progressContainer;
   private double lastAverageProgress;
   private double averageProgress;
   private double averageSpeed;
   private final Object workLock;

   public Downloader(Configuration.ConnectionQuality configuration) {
      super("MD");
      this.setConfiguration(configuration);
      this.remainingObjects = new AtomicInteger();
      this.threads = new DownloaderThread[8];
      this.list = Collections.synchronizedList(new ArrayList());
      this.listeners = Collections.synchronizedList(new ArrayList());
      this.speedContainer = new double[8];
      this.progressContainer = new double[8];
      this.workLock = new Object();
      this.start();
   }

   public Downloader(TLauncher tlauncher) {
      this(tlauncher.getSettings().getConnectionQuality());
   }

   public Configuration.ConnectionQuality getConfiguration() {
      return this.configuration;
   }

   public int getRemaining() {
      return this.remainingObjects.get();
   }

   public double getProgress() {
      return this.averageProgress;
   }

   public double getSpeed() {
      return this.averageSpeed;
   }

   public void add(Downloadable d) {
      if (d == null) {
         throw new NullPointerException();
      } else {
         this.list.add(d);
      }
   }

   public void add(DownloadableContainer c) {
      if (c == null) {
         throw new NullPointerException();
      } else {
         this.list.addAll(c.list);
      }
   }

   public void addAll(Downloadable... ds) {
      if (ds == null) {
         throw new NullPointerException();
      } else {
         for(int i = 0; i < ds.length; ++i) {
            if (ds[i] == null) {
               throw new NullPointerException("Downloadable at " + i + " is NULL!");
            }

            this.list.add(ds[i]);
         }

      }
   }

   public void addAll(Collection coll) {
      if (coll == null) {
         throw new NullPointerException();
      } else {
         int i = -1;
         Iterator var4 = coll.iterator();

         while(var4.hasNext()) {
            Downloadable d = (Downloadable)var4.next();
            ++i;
            if (d == null) {
               throw new NullPointerException("Downloadable at" + i + " is NULL!");
            }

            this.list.add(d);
         }

      }
   }

   public void addListener(DownloaderListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         this.listeners.add(listener);
      }
   }

   public void startDownload() {
      this.unblockThread("iteration");
   }

   public void startDownloadAndWait() {
      this.startDownload();
      this.waitWork();
   }

   private void waitWork() {
      synchronized(this.workLock) {
         try {
            this.workLock.wait();
         } catch (InterruptedException var3) {
         }

      }
   }

   private void notifyWork() {
      synchronized(this.workLock) {
         this.workLock.notifyAll();
      }
   }

   public void stopDownload() {
      if (!this.isThreadBlocked()) {
         throw new IllegalStateException();
      } else {
         DownloaderThread[] var4;
         int var3 = (var4 = this.threads).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            DownloaderThread thread = var4[var2];
            thread.stopDownload();
         }

      }
   }

   public void stopDownloadAndWait() {
      if (!this.isThreadBlocked()) {
         throw new IllegalStateException();
      } else {
         DownloaderThread[] var4;
         int var3 = (var4 = this.threads).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            DownloaderThread thread = var4[var2];
            thread.stopDownload();
         }

         this.waitForThreads();
      }
   }

   public void setConfiguration(Configuration.ConnectionQuality configuration) {
      if (configuration == null) {
         throw new NullPointerException();
      } else {
         this.configuration = configuration;
      }
   }

   public void run() {
      this.checkCurrent();
      boolean firstRun = true;

      while(true) {
         if (this.list.isEmpty()) {
            this.blockThread("iteration");
         }

         if (!this.list.isEmpty()) {
            synchronized(this.list) {
               this.sortOut();
            }

            for(int i = 0; i < this.runningThreads; ++i) {
               this.threads[i].startDownload();
            }

            if (!firstRun) {
               this.blockThread("download");
            }
         }

         this.notifyWork();
         Arrays.fill(this.speedContainer, 0.0D);
         Arrays.fill(this.progressContainer, 0.0D);
         this.averageProgress = 0.0D;
         this.lastAverageProgress = 0.0D;
         firstRun = false;
      }
   }

   private void sortOut() {
      int size = this.list.size();
      if (size != 0) {
         int downloadablesAtThread = U.getMaxMultiply(size, 8);
         int x = 0;
         int y = true;
         this.log("Starting download " + size + " files...");
         this.onStart(size);

         for(; size > 0; downloadablesAtThread = U.getMaxMultiply(size, 8)) {
            for(int i = 0; i < 8; ++i) {
               size -= downloadablesAtThread;
               if (this.threads[i] == null) {
                  this.threads[i] = new DownloaderThread(this, ++this.runningThreads);
               }

               int y;
               for(y = x; y < x + downloadablesAtThread; ++y) {
                  this.threads[i].add((Downloadable)this.list.get(y));
               }

               x = y;
               if (size == 0) {
                  break;
               }
            }
         }

         this.list.clear();
      }
   }

   private void onStart(int size) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         DownloaderListener listener = (DownloaderListener)var3.next();
         listener.onDownloaderStart(this, size);
      }

      this.remainingObjects.addAndGet(size);
   }

   void onAbort() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         DownloaderListener listener = (DownloaderListener)var2.next();
         listener.onDownloaderAbort(this);
      }

   }

   void onProgress(DownloaderThread thread, double curprogress, double curspeed) {
      int id = thread.getID() - 1;
      this.progressContainer[id] = curprogress;
      this.speedContainer[id] = curspeed;
      this.averageProgress = U.getAverage(this.progressContainer, this.runningThreads);
      if (!(this.averageProgress - this.lastAverageProgress < 0.01D)) {
         this.lastAverageProgress = this.averageProgress;
         this.averageSpeed = U.getSum(this.speedContainer);
         Iterator var8 = this.listeners.iterator();

         while(var8.hasNext()) {
            DownloaderListener listener = (DownloaderListener)var8.next();
            listener.onDownloaderProgress(this, this.averageProgress, this.averageSpeed);
         }

      }
   }

   void onFileComplete(DownloaderThread thread, Downloadable file) {
      int remaining = this.remainingObjects.decrementAndGet();
      Iterator var5 = this.listeners.iterator();

      while(var5.hasNext()) {
         DownloaderListener listener = (DownloaderListener)var5.next();
         listener.onDownloaderFileComplete(this, file);
      }

      if (remaining == 0) {
         this.onComplete();
      }

   }

   private void onComplete() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         DownloaderListener listener = (DownloaderListener)var2.next();
         listener.onDownloaderComplete(this);
      }

      this.unblockThread("download");
   }

   private void waitForThreads() {
      this.log("Waiting for threads...");

      for(int i = 0; i < this.runningThreads; ++i) {
         if (!this.threads[i].isThreadBlocked()) {
            i = -1;
         }
      }

      this.log("All threads are blocked by now");
   }

   private void log(Object... o) {
      U.log("[Downloader2]", o);
   }
}
