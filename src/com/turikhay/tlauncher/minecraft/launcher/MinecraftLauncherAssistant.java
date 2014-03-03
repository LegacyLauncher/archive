package com.turikhay.tlauncher.minecraft.launcher;

import com.turikhay.tlauncher.downloader.Downloader;

public abstract class MinecraftLauncherAssistant {
   protected final MinecraftLauncher launcher;

   protected MinecraftLauncherAssistant(MinecraftLauncher launcher) {
      if (launcher == null) {
         throw new NullPointerException();
      } else {
         this.launcher = launcher;
      }
   }

   public MinecraftLauncher getLauncher() {
      return this.launcher;
   }

   protected abstract void collectInfo() throws MinecraftException;

   protected abstract void collectResources(Downloader var1) throws MinecraftException;

   protected abstract void constructJavaArguments() throws MinecraftException;

   protected abstract void constructProgramArguments() throws MinecraftException;
}
