package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class LatestVersionSyncInfo extends VersionSyncInfo {
	private final ReleaseType type;

	public LatestVersionSyncInfo(ReleaseType type, Version localVersion,
			Version remoteVersion) {
		super(localVersion, remoteVersion);

		if (type == null)
			throw new NullPointerException("ReleaseType cannot be NULL!");

		this.type = type;
		this.setID("latest-" + type.toString());
	}

	public LatestVersionSyncInfo(ReleaseType type, VersionSyncInfo syncInfo) {
		this(type, syncInfo.getLocal(), syncInfo.getRemote());
	}

	public String getVersionID() {
		if (localVersion != null)
			return localVersion.getID();

		if (remoteVersion != null)
			return remoteVersion.getID();

		return null;
	}

	public ReleaseType getReleaseType() {
		return type;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{id='" + getID()
				+ "', releaseType=" + type + ",\nlocal=" + localVersion
				+ ",\nremote=" + remoteVersion + ", isInstalled="
				+ isInstalled() + ", hasRemote=" + hasRemote()
				+ ", isUpToDate=" + isUpToDate() + "}";
	}

}
