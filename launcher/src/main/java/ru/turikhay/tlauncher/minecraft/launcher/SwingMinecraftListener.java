package ru.turikhay.tlauncher.minecraft.launcher;

import ru.turikhay.tlauncher.minecraft.crash.CrashManager;
import ru.turikhay.util.SwingUtil;

import java.util.Objects;

public class SwingMinecraftListener implements MinecraftListener {
    private final MinecraftListener listener;

    public SwingMinecraftListener(MinecraftListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    @Override
    public void onMinecraftPrepare() {
        SwingUtil.later(listener::onMinecraftPrepare);
    }

    @Override
    public void onMinecraftAbort() {
        SwingUtil.later(listener::onMinecraftAbort);
    }

    @Override
    public void onMinecraftLaunch() {
        SwingUtil.later(listener::onMinecraftLaunch);
    }

    @Override
    public void onMinecraftClose() {
        SwingUtil.later(listener::onMinecraftClose);
    }

    @Override
    public void onMinecraftError(Throwable var1) {
        SwingUtil.later(() -> listener.onMinecraftError(var1));
    }

    @Override
    public void onMinecraftKnownError(MinecraftException var1) {
        SwingUtil.later(() -> listener.onMinecraftKnownError(var1));
    }

    @Override
    public void onCrashManagerInit(CrashManager manager) {
        SwingUtil.wait(() -> listener.onCrashManagerInit(manager));
    }
}
