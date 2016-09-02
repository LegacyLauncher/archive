package ru.turikhay.tlauncher.ui.versions;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.managers.VersionManager;

import java.util.List;

public interface VersionHandlerListener {
    void onVersionRefreshing(VersionManager var1);

    void onVersionRefreshed(VersionManager var1);

    void onVersionSelected(List<VersionSyncInfo> var1);

    void onVersionDeselected();

    void onVersionDownload(List<VersionSyncInfo> var1);
}
