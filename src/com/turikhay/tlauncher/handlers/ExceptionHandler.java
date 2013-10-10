package com.turikhay.tlauncher.handlers;

import com.turikhay.tlauncher.ui.Alert;
import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
   private static ExceptionHandler instance;

   public static ExceptionHandler getInstance() {
      if (instance == null) {
         instance = new ExceptionHandler();
      }

      return instance;
   }

   public ExceptionHandler() {
      if (instance != null) {
         throw new IllegalStateException("Use method ExceptionHandler.getInstance() instead of creating new instance.");
      } else {
         instance = this;
      }
   }

   public void uncaughtException(Thread t, Throwable e) {
      e.printStackTrace();

      try {
         Alert.showError("Exception in thread " + t.getName(), e);
      } catch (Exception var4) {
         System.exit(2);
      }

   }
}
