package net.legacylauncher.managers;

import net.legacylauncher.downloader.DownloadableContainer;
import net.minecraft.launcher.updater.VersionSyncInfo;

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
