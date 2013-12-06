package org.apache.commons.io.monitor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

public final class FileAlterationMonitor implements Runnable {
   private final long interval;
   private final List observers;
   private Thread thread;
   private ThreadFactory threadFactory;
   private volatile boolean running;

   public FileAlterationMonitor() {
      this(10000L);
   }

   public FileAlterationMonitor(long interval) {
      this.observers = new CopyOnWriteArrayList();
      this.thread = null;
      this.running = false;
      this.interval = interval;
   }

   public FileAlterationMonitor(long interval, FileAlterationObserver... observers) {
      this(interval);
      if (observers != null) {
         FileAlterationObserver[] var7 = observers;
         int var6 = observers.length;

         for(int var5 = 0; var5 < var6; ++var5) {
            FileAlterationObserver observer = var7[var5];
            this.addObserver(observer);
         }
      }

   }

   public long getInterval() {
      return this.interval;
   }

   public synchronized void setThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
   }

   public void addObserver(FileAlterationObserver observer) {
      if (observer != null) {
         this.observers.add(observer);
      }

   }

   public void removeObserver(FileAlterationObserver observer) {
      if (observer != null) {
         while(this.observers.remove(observer)) {
         }
      }

   }

   public Iterable getObservers() {
      return this.observers;
   }

   public synchronized void start() throws Exception {
      if (this.running) {
         throw new IllegalStateException("Monitor is already running");
      } else {
         Iterator var2 = this.observers.iterator();

         while(var2.hasNext()) {
            FileAlterationObserver observer = (FileAlterationObserver)var2.next();
            observer.initialize();
         }

         this.running = true;
         if (this.threadFactory != null) {
            this.thread = this.threadFactory.newThread(this);
         } else {
            this.thread = new Thread(this);
         }

         this.thread.start();
      }
   }

   public synchronized void stop() throws Exception {
      this.stop(this.interval);
   }

   public synchronized void stop(long stopInterval) throws Exception {
      if (!this.running) {
         throw new IllegalStateException("Monitor is not running");
      } else {
         this.running = false;

         try {
            this.thread.join(stopInterval);
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
         }

         Iterator var4 = this.observers.iterator();

         while(var4.hasNext()) {
            FileAlterationObserver observer = (FileAlterationObserver)var4.next();
            observer.destroy();
         }

      }
   }

   public void run() {
      while(true) {
         if (this.running) {
            Iterator var2 = this.observers.iterator();

            while(var2.hasNext()) {
               FileAlterationObserver observer = (FileAlterationObserver)var2.next();
               observer.checkAndNotify();
            }

            if (this.running) {
               try {
                  Thread.sleep(this.interval);
               } catch (InterruptedException var3) {
               }
               continue;
            }
         }

         return;
      }
   }
}
