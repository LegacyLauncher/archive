package com.turikhay.util;

import com.turikhay.tlauncher.handlers.ExceptionHandler;

public class AsyncThread extends Thread {
   private long wait;
   private Runnable runnable;

   private AsyncThread(Runnable r) {
      Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
      this.runnable = r;
   }

   private AsyncThread(Runnable r, long wait) {
      this(r);
      this.wait = wait;
   }

   public void run() {
      if (this.wait > 0L) {
         this.sleepFor(this.wait);
      }

      this.runnable.run();
      this.interrupt();
   }

   private void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var4) {
      }

   }

   public static void execute(Runnable r) {
      (new AsyncThread(r)).start();
   }

   public static void execute(Runnable r, long sleep) {
      (new AsyncThread(r, sleep)).start();
   }
}
