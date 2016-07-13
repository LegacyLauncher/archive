package ru.turikhay.tlauncher.minecraft.launcher;

public interface MinecraftExtendedListener extends MinecraftListener {
   void onMinecraftCollecting();

   void onMinecraftComparingAssets(boolean var1);

   void onMinecraftDownloading();

   void onMinecraftReconstructingAssets();

   void onMinecraftUnpackingNatives();

   void onMinecraftDeletingEntries();

   void onMinecraftConstructing();

   void onMinecraftPostLaunch();
}
