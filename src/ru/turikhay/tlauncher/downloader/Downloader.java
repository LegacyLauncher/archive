package ru.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public class Downloader extends ExtendedThread {
   private final DownloaderThread[] threads;
   private final List list;
   private final List listeners;
   private Configuration.ConnectionQuality configuration;
   private final AtomicInteger remainingObjects;
   private int runningThreads;
   private int workingThreads;
   private final double[] progressContainer;
   private double lastAverageProgress;
   private double averageProgress;
   private double lastProgress;
   private double speed;
   private boolean aborted;
   private final Object workLock;
   private boolean haveWork;

   private Downloader(Configuration.ConnectionQuality configuration) {
      super("MD");
      this.remainingObjects = new AtomicInteger();
      this.setConfiguration(configuration);
      this.threads = new DownloaderThread[6];
      this.list = Collections.synchronizedList(new ArrayList());
      this.listeners = Collections.synchronizedList(new ArrayList());
      this.progressContainer = new double[6];
      this.workLock = new Object();
      this.startAndWait();
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

   public double getLastProgress() {
      return this.lastProgress;
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

   public void addListener(DownloaderListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         this.listeners.add(listener);
      }
   }

   public boolean startDownload() {
      boolean haveWork = !this.list.isEmpty();
      if (haveWork) {
         this.unlockThread("iteration");
      }

      return haveWork;
   }

   public void startDownloadAndWait() {
      if (this.startDownload()) {
         this.waitWork();
      }

   }

   private void waitWork() {
      this.haveWork = true;

      while(this.haveWork) {
         Object var1 = this.workLock;
         synchronized(this.workLock) {
            try {
               this.workLock.wait();
            } catch (InterruptedException var5) {
               var5.printStackTrace();
            }
         }
      }

   }

   private void notifyWork() {
      this.haveWork = false;
      Object var1 = this.workLock;
      synchronized(this.workLock) {
         this.workLock.notifyAll();
      }
   }

   public void stopDownload() {
      if (!this.isThreadLocked()) {
         throw new IllegalArgumentException();
      } else {
         for(int i = 0; i < this.runningThreads; ++i) {
            this.threads[i].stopDownload();
         }

         this.aborted = true;
         if (this.isThreadLocked()) {
            this.tryUnlock("download");
         }

      }
   }

   public void stopDownloadAndWait() {
      this.stopDownload();
      this.waitForThreads();
   }

   public void setConfiguration(Configuration.ConnectionQuality configuration) {
      if (configuration == null) {
         throw new NullPointerException();
      } else {
         log("Loaded configuration:", configuration);
         this.configuration = configuration;
      }
   }

   public void run() {
      this.checkCurrent();

      while(true) {
         this.lockThread("iteration");
         log("Files in queue", this.list.size());
         List i = this.list;
         synchronized(this.list) {
            this.sortOut();
         }

         for(int var3 = 0; var3 < this.runningThreads; ++var3) {
            this.threads[var3].startDownload();
         }

         this.lockThread("download");
         if (this.aborted) {
            this.waitForThreads();
            this.onAbort();
            this.aborted = false;
         }

         this.notifyWork();
         Arrays.fill(this.progressContainer, 0.0D);
         this.speed = 0.0D;
         this.averageProgress = 0.0D;
         this.lastAverageProgress = 0.0D;
         this.workingThreads = 0;
         this.remainingObjects.set(0);
      }
   }

   private void sortOut() {
      int size = this.list.size();
      if (size != 0) {
         int downloadablesAtThread = U.getMaxMultiply(size, 6);
         int x = 0;
         boolean y = true;
         log("Starting download " + size + " files...");
         this.onStart(size);
         int max = this.configuration.getMaxThreads();

         boolean[] workers;
         int var11;
         for(workers = new boolean[max]; size > 0; downloadablesAtThread = U.getMaxMultiply(size, 6)) {
            for(int worker = 0; worker < max; ++worker) {
               workers[worker] = true;
               size -= downloadablesAtThread;
               if (this.threads[worker] == null) {
                  this.threads[worker] = new DownloaderThread(this, ++this.runningThreads);
               }

               for(var11 = x; var11 < x + downloadablesAtThread; ++var11) {
                  this.threads[worker].add((Downloadable)this.list.get(var11));
               }

               x = var11;
               if (size == 0) {
                  break;
               }
            }
         }

         boolean[] var10 = workers;
         var11 = workers.length;

         for(int var8 = 0; var8 < var11; ++var8) {
            boolean var12 = var10[var8];
            if (var12) {
               ++this.workingThreads;
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

   void onProgress(DownloaderThread thread, double curprogress, double curdone, double curspeed) {
      int id = thread.getID() - 1;
      this.lastProgress = curdone;
      this.progressContainer[id] = curprogress;
      this.averageProgress = U.getAverage(this.progressContainer, this.workingThreads);
      if (this.remainingObjects.get() == 1 || this.averageProgress - this.lastAverageProgress >= 0.01D) {
         this.speed = 0.005D * this.speed + 0.995D * curspeed;
         this.lastAverageProgress = this.averageProgress;
         Iterator var8 = this.listeners.iterator();

         while(var8.hasNext()) {
            DownloaderListener listener = (DownloaderListener)var8.next();
            listener.onDownloaderProgress(this, this.averageProgress, this.speed);
         }
      }

   }

   void onFileComplete(DownloaderThread thread, Downloadable file) {
      int remaining = this.remainingObjects.decrementAndGet();
      log("Objects remaining:", remaining);
      Iterator var5 = this.listeners.iterator();

      while(var5.hasNext()) {
         DownloaderListener listener = (DownloaderListener)var5.next();
         listener.onDownloaderFileComplete(this, file);
      }

      if (remaining < 1) {
         this.onComplete();
      }

   }

   private void onComplete() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         DownloaderListener listener = (DownloaderListener)var2.next();
         listener.onDownloaderComplete(this);
      }

      this.unlockThread("download");
   }

   private void waitForThreads() {
      log("Waiting for", this.workingThreads, "threads...");

      boolean blocked;
      do {
         blocked = true;

         for(int i = 0; i < this.workingThreads; ++i) {
            if (!this.threads[i].isThreadLocked()) {
               blocked = false;
            }
         }
      } while(!blocked);

      log("All threads are blocked by now");
   }

   private static void log(Object... o) {
      U.log("[Downloader2]", o);
   }
}
