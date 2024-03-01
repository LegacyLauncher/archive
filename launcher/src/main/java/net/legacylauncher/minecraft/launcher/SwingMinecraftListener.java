package net.legacylauncher.minecraft.launcher;

import net.legacylauncher.minecraft.crash.CrashManager;
import net.legacylauncher.util.SwingUtil;

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
    public void onMinecraftError(Throwable throwable) {
        SwingUtil.later(() -> listener.onMinecraftError(throwable));
    }

    @Override
    public void onMinecraftKnownError(MinecraftException exception) {
        SwingUtil.later(() -> listener.onMinecraftKnownError(exception));
    }

    @Override
    public void onCrashManagerInit(CrashManager manager) {
        SwingUtil.wait(() -> listener.onCrashManagerInit(manager));
    }
}
