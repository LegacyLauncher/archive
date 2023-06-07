package ru.turikhay.tlauncher.minecraft.launcher;

import ru.turikhay.util.SwingUtil;

public class SwingMinecraftExtendedListener extends SwingMinecraftListener implements MinecraftExtendedListener {
    private final MinecraftExtendedListener listener;

    public SwingMinecraftExtendedListener(MinecraftExtendedListener listener) {
        super(listener);
        this.listener = listener;
    }

    @Override
    public void onMinecraftCollecting() {
        SwingUtil.later(listener::onMinecraftCollecting);
    }

    @Override
    public void onMinecraftComparingAssets(boolean fast) {
        SwingUtil.later(() -> listener.onMinecraftComparingAssets(fast));
    }

    @Override
    public void onMinecraftCheckingJre() {
        SwingUtil.later(listener::onMinecraftCheckingJre);
    }

    @Override
    public void onMinecraftMalwareScanning() {
        SwingUtil.later(listener::onMinecraftMalwareScanning);
    }

    @Override
    public void onMinecraftDownloading() {
        SwingUtil.later(listener::onMinecraftDownloading);
    }

    @Override
    public void onMinecraftReconstructingAssets() {
        SwingUtil.later(listener::onMinecraftReconstructingAssets);
    }

    @Override
    public void onMinecraftUnpackingNatives() {
        SwingUtil.later(listener::onMinecraftUnpackingNatives);
    }

    @Override
    public void onMinecraftDeletingEntries() {
        SwingUtil.later(listener::onMinecraftDeletingEntries);
    }

    @Override
    public void onMinecraftConstructing() {
        SwingUtil.later(listener::onMinecraftConstructing);
    }

    @Override
    public void onMinecraftPostLaunch() {
        SwingUtil.later(listener::onMinecraftPostLaunch);
    }
}
