package com.turikhay.tlauncher.minecraft;

public interface MinecraftLauncherListener {
   void onMinecraftCheck();

   void onMinecraftPrepare();

   void onMinecraftLaunch();

   void onMinecraftClose();

   void onMinecraftError(MinecraftLauncherException var1);

   void onMinecraftError(Throwable var1);

   void onMinecraftWarning(String var1, Object var2);
}
