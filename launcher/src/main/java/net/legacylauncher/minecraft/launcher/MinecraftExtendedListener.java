package net.legacylauncher.minecraft.launcher;

public interface MinecraftExtendedListener extends MinecraftListener {
    void onMinecraftCollecting();

    void onMinecraftComparingAssets(boolean fast);

    void onMinecraftCheckingJre();

    void onMinecraftMalwareScanning();

    void onMinecraftDownloading();

    void onMinecraftReconstructingAssets();

    void onMinecraftUnpackingNatives();

    void onMinecraftDeletingEntries();

    void onMinecraftConstructing();

    void onMinecraftLaunch();

    void onMinecraftPostLaunch();
}
