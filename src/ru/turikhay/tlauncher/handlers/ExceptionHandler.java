package ru.turikhay.tlauncher.handlers;

import java.lang.Thread.UncaughtExceptionHandler;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.background.slide.SlideBackgroundThread;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class ExceptionHandler implements UncaughtExceptionHandler {
   private static ExceptionHandler instance;
   private static long gcLastCall;

   public static ExceptionHandler getInstance() {
      if (instance == null) {
         instance = new ExceptionHandler();
      }

      return instance;
   }

   public void uncaughtException(Thread t, Throwable e) {
      OutOfMemoryError asOOM = (OutOfMemoryError)Reflect.cast(e, OutOfMemoryError.class);
      if (asOOM == null || !reduceMemory(asOOM)) {
         if (scanTrace(e)) {
            try {
               Alert.showError("Exception in thread " + t.getName(), (Object)e);
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         } else {
            U.log("Hidden exception in thread " + t.getName(), e);
         }
      }

   }

   public static boolean reduceMemory(OutOfMemoryError e) {
      if (e == null) {
         return false;
      } else {
         U.log("OutOfMemory error has occurred, solving...");
         SlideBackgroundThread.alert();
         long currentTime = System.currentTimeMillis();
         long diff = Math.abs(currentTime - gcLastCall);
         if (diff > 5000L) {
            gcLastCall = currentTime;
            U.gc();
            SlideBackgroundThread.alert();
            return true;
         } else {
            U.log("GC is unable to reduce memory usage");
            return false;
         }
      }
   }

   private static boolean scanTrace(Throwable e) {
      StackTraceElement[] elements = e.getStackTrace();
      StackTraceElement[] var5 = elements;
      int var4 = elements.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         StackTraceElement element = var5[var3];
         if (element.getClassName().startsWith("ru.turikhay")) {
            return true;
         }
      }

      return false;
   }
}
