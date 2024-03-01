package net.legacylauncher.minecraft.launcher;

import net.legacylauncher.minecraft.crash.CrashManager;

public interface MinecraftListener {
    void onMinecraftPrepare();

    void onMinecraftAbort();

    void onMinecraftLaunch();

    void onMinecraftClose();

    void onMinecraftError(Throwable throwable);

    void onMinecraftKnownError(MinecraftException exception);

    void onCrashManagerInit(CrashManager manager);
}
