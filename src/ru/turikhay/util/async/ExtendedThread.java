package ru.turikhay.util.async;

import java.util.concurrent.atomic.AtomicInteger;
import ru.turikhay.util.U;

public abstract class ExtendedThread extends Thread {
   private static AtomicInteger threadNum = new AtomicInteger();
   private final ExtendedThread.ExtendedThreadCaller caller;
   private String blockReason;
   private final Object monitor;

   public ExtendedThread(String name) {
      super((name == null ? "ExtendedThread" : name) + "#" + threadNum.incrementAndGet());
      this.monitor = new Object();
      this.caller = new ExtendedThread.ExtendedThreadCaller();
   }

   public ExtendedThread() {
      this("ExtendedThread");
   }

   public ExtendedThread.ExtendedThreadCaller getCaller() {
      return this.caller;
   }

   public void startAndWait() {
      super.start();

      while(!this.isThreadLocked()) {
         U.sleepFor(100L);
      }

   }

   public abstract void run();

   protected void lockThread(String reason) {
      if (reason == null) {
         throw new NullPointerException();
      } else {
         this.checkCurrent();
         this.blockReason = reason;
         synchronized(this.monitor) {
            while(this.blockReason != null) {
               try {
                  this.monitor.wait();
               } catch (InterruptedException var5) {
                  var5.printStackTrace();
               }
            }

         }
      }
   }

   public void unlockThread(String reason) {
      if (reason == null) {
         throw new NullPointerException();
      } else if (!reason.equals(this.blockReason)) {
         throw new IllegalStateException("Unlocking denied! Locked with: " + this.blockReason + ", tried to unlock with: " + reason);
      } else {
         this.blockReason = null;
         Object var2 = this.monitor;
         synchronized(this.monitor) {
            this.monitor.notifyAll();
         }
      }
   }

   public void tryUnlock(String reason) {
      if (reason == null) {
         throw new NullPointerException();
      } else {
         if (reason.equals(this.blockReason)) {
            this.unlockThread(reason);
         }

      }
   }

   public boolean isThreadLocked() {
      return this.blockReason != null;
   }

   public boolean isCurrent() {
      return Thread.currentThread().equals(this);
   }

   protected void checkCurrent() {
      if (!this.isCurrent()) {
         throw new IllegalStateException("Illegal thread!");
      }
   }

   protected void threadLog(Object... o) {
      U.log("[" + this.getName() + "]", o);
   }

   public class ExtendedThreadCaller extends RuntimeException {
      private ExtendedThreadCaller() {
      }

      // $FF: synthetic method
      ExtendedThreadCaller(Object x1) {
         this();
      }
   }
}
