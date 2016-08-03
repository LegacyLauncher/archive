package ru.turikhay.util.async;

import javafx.application.Platform;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;

public abstract class FxRunnable implements Runnable {
   final Runnable fxBridge = new Runnable() {
      public void run() {
         try {
            FxRunnable.this.runFx();
         } catch (Throwable var2) {
            ExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), var2);
         }

      }
   };

   public void run() {
      if (Platform.isFxApplicationThread()) {
         this.fxBridge.run();
      } else {
         Platform.runLater(this.fxBridge);
      }

   }

   public abstract void runFx();
}
