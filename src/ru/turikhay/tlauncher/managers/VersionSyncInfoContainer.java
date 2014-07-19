package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import net.minecraft.launcher.updater.VersionSyncInfo;

public class VersionSyncInfoContainer extends DownloadableContainer {
	private final VersionSyncInfo version;
	
	public VersionSyncInfoContainer(VersionSyncInfo version) {
		if(version == null)
			throw new NullPointerException();
		
		this.version = version;
	}
	
	public VersionSyncInfo getVersion() {
		return version;
	}

}
