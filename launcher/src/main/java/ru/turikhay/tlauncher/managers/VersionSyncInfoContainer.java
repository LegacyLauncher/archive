package ru.turikhay.tlauncher.managers;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;

public class VersionSyncInfoContainer extends DownloadableContainer {
    private final VersionSyncInfo version;

    public VersionSyncInfoContainer(VersionSyncInfo version) {
        if (version == null) {
            throw new NullPointerException();
        } else {
            this.version = version;
        }
    }

    public VersionSyncInfo getVersion() {
        return version;
    }
}
