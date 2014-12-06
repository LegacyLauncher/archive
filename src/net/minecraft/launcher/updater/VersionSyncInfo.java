package net.minecraft.launcher.updater;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;

public class VersionSyncInfo {
	protected Version localVersion, remoteVersion;
	private CompleteVersion completeLocal, completeRemote;
	private String id;

	public VersionSyncInfo(Version localVersion, Version remoteVersion) {
		if (localVersion == null && remoteVersion == null)
			throw new NullPointerException(
					"Cannot create sync info from NULLs!");

		this.localVersion = localVersion;
		this.remoteVersion = remoteVersion;

		if(!(localVersion == null || remoteVersion == null))
			localVersion.setVersionList(remoteVersion.getVersionList());

		if (getID() == null)
			throw new NullPointerException(
					"Cannot create sync info from versions that have NULL IDs");
	}

	public VersionSyncInfo(VersionSyncInfo info) {
		this(info.getLocal(), info.getRemote());
	}

	private VersionSyncInfo() {
		this.localVersion = null;
		this.remoteVersion = null;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;

		if(getID() == null || o == null || !(o instanceof VersionSyncInfo))
			return false;

		VersionSyncInfo v = (VersionSyncInfo) o;

		return getID().equals(v.getID());
	}

	public Version getLocal() {
		return localVersion;
	}

	public void setLocal(Version version) {
		this.localVersion = version;
	}

	public Version getRemote() {
		return remoteVersion;
	}

	public void setRemote(Version version) {
		this.remoteVersion = version;
	}

	public String getID() {
		if (id != null)
			return id;

		if (localVersion != null)
			return localVersion.getID();

		if (remoteVersion != null)
			return remoteVersion.getID();

		return null;
	}

	public void setID(String id) {
		if (id != null && id.isEmpty())
			throw new IllegalArgumentException("ID cannot be empty!");

		this.id = id;
	}

	public Version getLatestVersion() {
		if (remoteVersion != null)
			return remoteVersion;

		return localVersion;
	}

	public Version getAvailableVersion() {
		if (localVersion != null)
			return localVersion;

		return remoteVersion;
	}

	public boolean isInstalled() {
		return localVersion != null;
	}

	public boolean hasRemote() {
		return remoteVersion != null;
	}

	public boolean isUpToDate() {
		if (localVersion == null)
			return false;
		if (remoteVersion == null)
			return true;

		return localVersion.getUpdatedTime().compareTo(
				remoteVersion.getUpdatedTime()) >= 0;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{id='" + getID() + "',\nlocal="
				+ localVersion + ",\nremote=" + remoteVersion
				+ ", isInstalled=" + isInstalled() + ", hasRemote="
				+ hasRemote() + ", isUpToDate=" + isUpToDate() + "}";
	}

	public CompleteVersion resolveCompleteVersion(VersionManager manager, boolean latest) throws IOException {
		Version version;

		if (latest)
			version = getLatestVersion();
		else if (isInstalled())
			version = getLocal();
		else
			version = getRemote();

		if (version.equals(localVersion) && completeLocal != null && completeLocal.getInheritsFrom() == null)
			return completeLocal;
		if (version.equals(remoteVersion) && completeRemote != null && completeRemote.getInheritsFrom() == null)
			return completeRemote;

		CompleteVersion complete = version.getVersionList().getCompleteVersion(version).resolve(manager, latest);

		if (version.equals(localVersion))
			this.completeLocal = complete;

		else if (version.equals(remoteVersion))
			this.completeRemote = complete;

		return complete;
	}

	public CompleteVersion getCompleteVersion(boolean latest)
			throws IOException {
		Version version;

		if (latest)
			version = getLatestVersion();
		else if (isInstalled())
			version = getLocal();
		else
			version = getRemote();

		if (version.equals(localVersion) && completeLocal != null)
			return completeLocal;
		if (version.equals(remoteVersion) && completeRemote != null)
			return completeRemote;

		CompleteVersion complete = version.getVersionList().getCompleteVersion(version);

		if (version.equals(localVersion))
			this.completeLocal = complete;

		else if (version.equals(remoteVersion))
			this.completeRemote = complete;

		return complete;
	}

	public CompleteVersion getLatestCompleteVersion() throws IOException {
		return getCompleteVersion(true);
	}

	public CompleteVersion getLocalCompleteVersion() {
		return completeLocal;
	}

	Set<Downloadable> getRequiredDownloadables(OS os, File targetDirectory, boolean force) throws IOException {
		Set<Downloadable> neededFiles = new HashSet<Downloadable>();

		CompleteVersion version = getCompleteVersion(force);
		Repository source = hasRemote() ? remoteVersion.getSource() : Repository.OFFICIAL_VERSION_REPO;

		if (!source.isSelectable())
			return neededFiles;

		for (Library library : version.getRelevantLibraries()) {
			String file = null;

			if (library.getNatives() != null) {
				String natives = library.getNatives().get(os);
				if (natives != null)
					file = library.getArtifactPath(natives);
			} else {
				file = library.getArtifactPath();
			}

			if (file == null)
				continue;

			File local = new File(targetDirectory, "libraries/" + file);

			if (!force && local.isFile() && local.length() > 0)
				continue;

			neededFiles.add(library.getDownloadable(source, local, os));
		}

		return neededFiles;
	}

	public Set<Downloadable> getRequiredDownloadables(File targetDirectory, boolean force) throws IOException {
		return getRequiredDownloadables(OS.CURRENT, targetDirectory, force);
	}

	public static VersionSyncInfo createEmpty() {
		return new VersionSyncInfo();
	}
}
