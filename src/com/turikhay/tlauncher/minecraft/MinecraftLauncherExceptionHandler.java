package com.turikhay.tlauncher.minecraft;

import java.lang.Thread.UncaughtExceptionHandler;

public class MinecraftLauncherExceptionHandler implements UncaughtExceptionHandler {
   private MinecraftLauncher l;

   MinecraftLauncherExceptionHandler(MinecraftLauncher l) {
      this.l = l;
   }

   public void uncaughtException(Thread t, Throwable e) {
      e.printStackTrace();
      this.l.onError(e);
   }
}
