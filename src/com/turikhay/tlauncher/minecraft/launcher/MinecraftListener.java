package com.turikhay.tlauncher.minecraft.launcher;

import com.turikhay.tlauncher.minecraft.crash.Crash;

public interface MinecraftListener {
   void onMinecraftPrepare();

   void onMinecraftAbort();

   void onMinecraftLaunch();

   void onMinecraftClose();

   void onMinecraftError(Throwable var1);

   void onMinecraftKnownError(MinecraftException var1);

   void onMinecraftCrash(Crash var1);
}
