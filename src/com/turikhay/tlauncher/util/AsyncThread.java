package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.handlers.ExceptionHandler;

public class AsyncThread extends Thread {
   private long wait;
   private Runnable runnable;

   public AsyncThread(Runnable r) {
      Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
      this.runnable = r;
   }

   public AsyncThread(Runnable r, long wait) {
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

   public static void execute(Runnable r) {
      (new AsyncThread(r)).start();
   }

   public static void execute(Runnable r, long sleep) {
      (new AsyncThread(r, sleep)).start();
   }

   private void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var4) {
      }

   }
}
