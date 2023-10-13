package net.legacylauncher.ui.versions;

import net.legacylauncher.managers.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;

import java.util.List;

public interface VersionHandlerListener {
    void onVersionRefreshing(VersionManager var1);

    void onVersionRefreshed(VersionManager var1);

    void onVersionSelected(List<VersionSyncInfo> var1);

    void onVersionDeselected();

    void onVersionDownload(List<VersionSyncInfo> var1);
}
