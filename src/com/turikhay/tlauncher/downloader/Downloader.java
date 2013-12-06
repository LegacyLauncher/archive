package com.turikhay.tlauncher.downloader;

import com.turikhay.util.U;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Downloader extends Thread {
   public final String name;
   public final int maxThreads;
   private boolean launched;
   private boolean available;
   private boolean list_;
   private boolean listeners_;
   private DownloaderThread[] threads;
   private List list;
   private List queue;
   private List listeners;
   private boolean[] running;
   private int[] remain;
   private int[] progress;
   private double[] speed;
   private int av_progress;
   private double av_speed;

   public Downloader(String name, int mthreads) {
      this.available = true;
      this.list_ = true;
      this.listeners_ = true;
      this.list = new ArrayList();
      this.queue = new ArrayList();
      this.listeners = new ArrayList();
      this.name = name;
      this.maxThreads = mthreads;
      this.threads = new DownloaderThread[this.maxThreads];
      this.running = new boolean[this.maxThreads];
      this.start();
   }

   public Downloader(int mthreads) {
      this("", mthreads);
   }

   public void run() {
      this.check();

      while(!this.launched) {
         this.sleepFor(500L);
      }

      while(!this.list_) {
         this.sleepFor(100L);
      }

      this.available = false;
      this.list_ = false;
      this.list.addAll(this.queue);
      this.queue.clear();
      this.av_progress = 0;
      this.remain = new int[this.maxThreads];
      this.progress = new int[this.maxThreads];
      this.speed = new double[this.maxThreads];
      int len = this.list.size();
      int each = U.getMaxMultiply(len, this.maxThreads);
      int x = 0;
      int y = -1;
      U.log("EACH:", each);
      if (len > 0) {
         this.onDownloaderStart(len);
      }

      int i;
      for(; len > 0; each = U.getMaxMultiply(len, this.maxThreads)) {
         for(i = 0; i < this.maxThreads; ++i) {
            ++y;
            len -= each;
            int[] var10000 = this.remain;
            var10000[i] += each;
            DownloaderThread curthread = this.threads[i];
            if (curthread == null) {
               this.running[i] = (curthread = this.threads[i] = new DownloaderThread(this, i)) != null;
            }

            for(y = x; y < x + each; ++y) {
               curthread.add((Downloadable)this.list.get(y));
            }

            x = y;
            if (len == 0) {
               break;
            }
         }
      }

      for(i = 0; i < this.maxThreads; ++i) {
         if (this.running[i]) {
            this.threads[i].startLaunch();
         }
      }

      this.list.clear();
      this.list_ = true;
      this.launched = false;

      while(!this.launched) {
         this.sleepFor(500L);
      }

      while(!this.list_) {
         this.sleepFor(100L);
      }

      this.list_ = false;
      this.list.addAll(this.queue);
      this.queue.clear();
      this.list_ = true;
      this.available = true;
      this.run();
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

   public void add(DownloadableContainer c) {
      while(!this.list_) {
         this.sleepFor(100L);
      }

      this.list_ = false;
      if (this.available) {
         this.list.addAll(c.elems);
      } else {
         this.queue.addAll(c.elems);
      }

      this.list_ = true;
   }

   public void addListener(DownloadListener l) {
      while(!this.listeners_) {
         this.sleepFor(100L);
      }

      this.listeners_ = false;
      this.listeners.add(l);
      this.listeners_ = true;
   }

   public void removeListener(DownloadListener l) {
      while(!this.listeners_) {
         this.sleepFor(100L);
      }

      this.listeners_ = false;
      this.listeners.remove(l);
      this.listeners_ = true;
   }

   public boolean isAvailable() {
      return this.available;
   }

   public boolean isLaunched() {
      return this.launched;
   }

   public int getRemaining() {
      return U.getSum(this.remain);
   }

   public void startLaunch() {
      this.launched = true;
   }

   public void stopLaunch() {
      DownloaderThread[] var4;
      int var3 = (var4 = this.threads).length;

      for(int var2 = 0; var2 < var3; ++var2) {
         DownloaderThread thread = var4[var2];
         if (thread != null) {
            thread.stopLaunch();
         }
      }

      boolean has = true;

      while(true) {
         while(has) {
            has = false;
            DownloaderThread[] var5;
            int var10 = (var5 = this.threads).length;

            for(var3 = 0; var3 < var10; ++var3) {
               DownloaderThread thread = var5[var3];
               if (thread != null && !thread.isAvailable()) {
                  has = true;
                  break;
               }
            }
         }

         Iterator var9 = this.listeners.iterator();

         while(var9.hasNext()) {
            DownloadListener l = (DownloadListener)var9.next();
            l.onDownloaderAbort(this);
         }

         return;
      }
   }

   void onDownloaderStart(int files) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         DownloadListener l = (DownloadListener)var3.next();
         l.onDownloaderStart(this, files);
      }

   }

   void onDownloaderStop() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         DownloadListener l = (DownloadListener)var2.next();
         l.onDownloaderComplete(this);
      }

   }

   void onStart(int id, Downloadable d) {
   }

   void onError(int id, Downloadable d) {
      int var10002 = this.remain[id]--;
      Throwable error = this.threads[id].getError();
      Iterator var5 = this.listeners.iterator();

      while(var5.hasNext()) {
         DownloadListener l = (DownloadListener)var5.next();
         l.onDownloaderError(this, d, error);
      }

   }

   void onProgress(int id, int curprogress, double curspeed) {
      this.progress[id] = curprogress;
      this.speed[id] = curspeed;
      int old_progress = this.av_progress;
      this.av_progress = U.getAverage(this.progress);
      if (this.av_progress != old_progress) {
         this.av_speed = U.getSum(this.speed);
         Iterator var7 = this.listeners.iterator();

         while(var7.hasNext()) {
            DownloadListener l = (DownloadListener)var7.next();
            l.onDownloaderProgress(this, this.av_progress, this.av_speed);
         }

      }
   }

   void onComplete(int id, Downloadable d) {
      int var10002 = this.remain[id]--;
      Iterator var4 = this.listeners.iterator();

      DownloadListener l;
      while(var4.hasNext()) {
         l = (DownloadListener)var4.next();
         l.onDownloaderFileComplete(this, d);
      }

      int[] var6;
      int var5 = (var6 = this.remain).length;

      for(int var8 = 0; var8 < var5; ++var8) {
         int curremain = var6[var8];
         if (curremain != 0) {
            return;
         }
      }

      var4 = this.listeners.iterator();

      while(var4.hasNext()) {
         l = (DownloadListener)var4.next();
         l.onDownloaderComplete(this);
      }

   }

   private void check() {
      if (!this.available) {
         throw new IllegalStateException("Downloader is unavailable!");
      }
   }

   private void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var4) {
      }

   }
}
