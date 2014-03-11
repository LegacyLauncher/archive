package com.turikhay.tlauncher.handlers;

import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.util.U;
import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
   private static ExceptionHandler instance;
   private static long gcLastCall;

   public static ExceptionHandler getInstance() {
      if (instance == null) {
         instance = new ExceptionHandler();
      }

      return instance;
   }

   private ExceptionHandler() {
   }

   public void uncaughtException(Thread t, Throwable e) {
      if (!(e instanceof OutOfMemoryError) || !reduceMemory((OutOfMemoryError)e)) {
         try {
            Alert.showError("Exception in thread " + t.getName(), (Object)e);
         } catch (Exception var4) {
            System.exit(2);
         }

      }
   }

   public static boolean reduceMemory(OutOfMemoryError e) {
      U.log("OutOfMemory error has occurred, solving...");
      long currentTime = System.currentTimeMillis();
      long diff = Math.abs(currentTime - gcLastCall);
      if (diff > 5000L) {
         gcLastCall = currentTime;
         U.gc();
         return true;
      } else {
         U.log("GC is unable to reduce memory usage");
         return false;
      }
   }
}
