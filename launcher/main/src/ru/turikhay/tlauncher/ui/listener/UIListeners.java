package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;

public final class UIListeners {
    private final MinecraftUIListener minecraftUIListener;
    private final VersionManagerUIListener versionManagerUIListener;
    private final RequiredUpdateListener requiredUpdateListener;

    public UIListeners(TLauncher tlauncher) {
        this.minecraftUIListener = new MinecraftUIListener(tlauncher);
        this.versionManagerUIListener = new VersionManagerUIListener(tlauncher);
        this.requiredUpdateListener = new RequiredUpdateListener(tlauncher.getUpdater());
    }

    public MinecraftUIListener getMinecraftUIListener() {
        return minecraftUIListener;
    }

    public VersionManagerUIListener getVersionManagerUIListener() {
        return versionManagerUIListener;
    }

    public RequiredUpdateListener getRequiredUpdateListener() {
        return requiredUpdateListener;
    }
}
