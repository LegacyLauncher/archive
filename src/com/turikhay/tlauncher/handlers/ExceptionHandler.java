package com.turikhay.tlauncher.handlers;

import com.turikhay.tlauncher.ui.alert.Alert;
import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
   private static ExceptionHandler instance;

   public static ExceptionHandler getInstance() {
      if (instance == null) {
         instance = new ExceptionHandler();
      }

      return instance;
   }

   private ExceptionHandler() {
   }

   public void uncaughtException(Thread t, Throwable e) {
      e.printStackTrace();

      try {
         Alert.showError("Exception in thread " + t.getName(), (Object)e);
      } catch (Exception var4) {
         System.exit(2);
      }

   }
}
