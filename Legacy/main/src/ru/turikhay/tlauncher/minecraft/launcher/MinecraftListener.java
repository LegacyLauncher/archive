package ru.turikhay.tlauncher.minecraft.launcher;

import ru.turikhay.tlauncher.minecraft.crash.CrashManager;

public interface MinecraftListener {
    void onMinecraftPrepare();

    void onMinecraftAbort();

    void onMinecraftLaunch();

    void onMinecraftClose();

    void onMinecraftError(Throwable var1);

    void onMinecraftKnownError(MinecraftException var1);

    void onCrashManagerInit(CrashManager manager);
}
