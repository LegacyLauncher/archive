package ru.turikhay.tlauncher.ui.versions;

import java.util.List;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.managers.VersionManager;

public interface VersionHandlerListener {
	void onVersionRefreshing(VersionManager vm);
	void onVersionRefreshed(VersionManager vm);
	void onVersionSelected(List<VersionSyncInfo> versions);
	void onVersionDeselected();
	void onVersionDownload(List<VersionSyncInfo> list);
}
