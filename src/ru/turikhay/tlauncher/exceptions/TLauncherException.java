package ru.turikhay.tlauncher.exceptions;

public class TLauncherException extends RuntimeException {
   public TLauncherException(String message, Throwable e) {
      super(message, e);
      e.printStackTrace();
   }

   public TLauncherException(String message) {
      super(message);
   }
}
