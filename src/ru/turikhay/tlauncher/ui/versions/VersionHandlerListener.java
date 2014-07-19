package ru.turikhay.tlauncher.ui.versions;

import java.util.List;

import ru.turikhay.tlauncher.managers.VersionManager;

import net.minecraft.launcher.updater.VersionSyncInfo;

public interface VersionHandlerListener {
	void onVersionRefreshing(VersionManager vm);
	void onVersionRefreshed(VersionManager vm);
	void onVersionSelected(List<VersionSyncInfo> versions);
	void onVersionDeselected();
	void onVersionDownload(List<VersionSyncInfo> list);
}
