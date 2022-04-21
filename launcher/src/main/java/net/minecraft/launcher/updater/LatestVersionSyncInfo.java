package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class LatestVersionSyncInfo extends VersionSyncInfo {
    private final ReleaseType type;

    public LatestVersionSyncInfo(ReleaseType type, Version localVersion, Version remoteVersion) {
        super(localVersion, remoteVersion);
        if (type == null) {
            throw new NullPointerException("ReleaseType cannot be NULL!");
        } else {
            this.type = type;
            setID("latest-" + type);
        }
    }

    public LatestVersionSyncInfo(ReleaseType type, VersionSyncInfo syncInfo) {
        this(type, syncInfo.getLocal(), syncInfo.getRemote());
    }

    public String getVersionID() {
        return localVersion != null ? localVersion.getID() : (remoteVersion != null ? remoteVersion.getID() : null);
    }

    public ReleaseType getReleaseType() {
        return type;
    }

    public String toString() {
        return getClass().getSimpleName() + "{id='" + getID() + "', releaseType=" + type + ",\nlocal=" + localVersion + ",\nremote=" + remoteVersion + ", isInstalled=" + isInstalled() + ", hasRemote=" + hasRemote() + ", isUpToDate=" + isUpToDate() + "}";
    }
}
