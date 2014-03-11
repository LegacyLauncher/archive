package com.turikhay.util.async;

import com.turikhay.util.U;

public abstract class ExtendedThread extends Thread {
   private static int threadNum;
   private final ExtendedThread.ExtendedThreadCaller caller;
   private String blockReason;

   public ExtendedThread(String name) {
      super(name + "#" + threadNum++);
      this.caller = new ExtendedThread.ExtendedThreadCaller();
   }

   public ExtendedThread() {
      this("ExtendedThread");
   }

   public ExtendedThread.ExtendedThreadCaller getCaller() {
      return this.caller;
   }

   public abstract void run();

   protected synchronized void blockThread(String reason) {
      if (reason == null) {
         throw new NullPointerException();
      } else {
         this.checkCurrent();
         this.blockReason = reason;
         this.threadLog("Thread locked by:", this.blockReason);

         while(this.blockReason != null) {
            try {
               this.wait();
            } catch (InterruptedException var3) {
               return;
            }
         }

         this.threadLog("Thread has been unlocked");
      }
   }

   public synchronized void unblockThread(String reason) {
      if (reason == null) {
         throw new NullPointerException();
      } else if (this.blockReason != null && this.blockReason.equals(reason)) {
         this.blockReason = null;
         this.notifyAll();
      } else {
         throw new IllegalStateException("Unlocking denied! Locked with: " + this.blockReason + ", tried to unlock with: " + reason);
      }
   }

   public boolean isThreadBlocked() {
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

   void threadLog(Object... o) {
      U.log("[" + this.getName() + "]", o);
   }

   public class ExtendedThreadCaller extends RuntimeException {
      private static final long serialVersionUID = -9184403765829112550L;
   }
}
