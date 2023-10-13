package net.legacylauncher.minecraft.launcher;

import net.legacylauncher.minecraft.crash.CrashManager;

public interface MinecraftListener {
    void onMinecraftPrepare();

    void onMinecraftAbort();

    void onMinecraftLaunch();

    void onMinecraftClose();

    void onMinecraftError(Throwable var1);

    void onMinecraftKnownError(MinecraftException var1);

    void onCrashManagerInit(CrashManager manager);
}
