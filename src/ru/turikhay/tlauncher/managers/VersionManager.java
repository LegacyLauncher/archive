package ru.turikhay.tlauncher.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.launcher.updater.LatestVersionSyncInfo;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionList.RawVersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncObject;
import ru.turikhay.util.async.AsyncObjectContainer;
import ru.turikhay.util.async.AsyncObjectGotErrorException;
import ru.turikhay.util.async.AsyncThread;

@ComponentDependence({ AssetsManager.class, VersionLists.class })
public class VersionManager extends InterruptibleComponent {

	private final LocalVersionList localList;
	private final RemoteVersionList[] remoteLists;

	private Map<ReleaseType, Version> latestVersions;

	private final List<VersionManagerListener> listeners;
	private final Object versionFlushLock;

	public VersionManager(ComponentManager manager) throws Exception {
		super(manager);

		VersionLists list = manager.getComponent(VersionLists.class);

		this.localList = list.getLocal();
		this.remoteLists = list.getRemoteLists();

		this.latestVersions = new LinkedHashMap<ReleaseType, Version>();

		this.listeners = Collections
				.synchronizedList(new ArrayList<VersionManagerListener>());
		this.versionFlushLock = new Object();
	}

	public void addListener(VersionManagerListener listener) {
		if (listener == null)
			throw new NullPointerException();

		this.listeners.add(listener);
	}

	public LocalVersionList getLocalList() {
		return localList;
	}

	public Map<ReleaseType, Version> getLatestVersions() {
		synchronized (versionFlushLock) {
			return Collections.unmodifiableMap(latestVersions);
		}
	}

	boolean refresh(int refreshID, boolean local) {
		this.refreshList[refreshID] = true;

		log("Refreshing versions...");

		if(!local) {
			latestVersions.clear();

			for (VersionManagerListener listener : listeners)
				listener.onVersionsRefreshing(this);
		}

		Object lock = new Object();
		Time.start(lock);

		Map<AsyncObject<RawVersionList>, RawVersionList> result = null;
		Throwable e = null;

		try {
			result = refreshVersions(local);
		} catch (Throwable e0) {
			e = e0;
		}

		if (isCancelled(refreshID)) {
			log("Version refresh has been cancelled (" + Time.stop(lock)+ " ms)");
			return false;
		}

		if (e != null) {
			for (VersionManagerListener listener : listeners)
				listener.onVersionsRefreshingFailed(this);

			log("Cannot refresh versions (" + Time.stop(lock) + " ms)", e);
			return true;
		}

		if (result != null)
			synchronized (versionFlushLock) {
				for (AsyncObject<RawVersionList> object : result.keySet()) {
					RawVersionList rawList = result.get(object);

					if (rawList == null)
						continue;

					AsyncRawVersionListObject listObject = (AsyncRawVersionListObject) object;
					RemoteVersionList versionList = listObject.getVersionList();

					versionList.refreshVersions(rawList);
					latestVersions.putAll(versionList.getLatestVersions());
				}
			}

		latestVersions = U.sortMap(latestVersions, ReleaseType.values());

		log("Versions has been refreshed (" + Time.stop(lock) + " ms)");

		this.refreshList[refreshID] = false;

		for (VersionManagerListener listener : listeners)
			listener.onVersionsRefreshed(this);

		return true;
	}

	@Override
	protected boolean refresh(int queueID) {
		return refresh(queueID, false);
	}

	public void startRefresh(boolean local) {
		this.refresh(nextID(), local);
	}

	@Override
	public synchronized void stopRefresh() {
		super.stopRefresh();
		this.startRefresh(true);
	}

	public void asyncRefresh(final boolean local) {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				startRefresh(local);
			}
		});
	}

	public void asyncRefresh() {
		asyncRefresh(false);
	}

	private Map<AsyncObject<RawVersionList>, RawVersionList> refreshVersions(boolean local) throws IOException {
		this.localList.refreshVersions();

		if (local)
			return null;

		AsyncObjectContainer<RawVersionList> container = new AsyncObjectContainer<RawVersionList>();

		for (RemoteVersionList remoteList : remoteLists)
			container.add(new AsyncRawVersionListObject(remoteList));

		return container.execute();
	}

	public void updateVersionList() {
		for (VersionManagerListener listener : listeners)
			listener.onVersionsRefreshed(this);
	}

	public VersionSyncInfo getVersionSyncInfo(Version version) {
		return getVersionSyncInfo(version.getID());
	}

	public VersionSyncInfo getVersionSyncInfo(String name) {
		if (name == null)
			throw new NullPointerException("Cannot get sync info of NULL!");

		if (name.startsWith("latest-")) {
			String realID = name.substring(7);
			name = null;

			for (Entry<ReleaseType, Version> entry : latestVersions.entrySet()) {
				if (entry.getKey().toString().equals(realID)) {
					name = entry.getValue().getID();
					break;
				}
			}

			if (name == null)
				return null;
		}

		Version localVersion = localList.getVersion(name);

		if(localVersion instanceof CompleteVersion && ((CompleteVersion) localVersion).getInheritsFrom() != null) {

			try {
				localVersion = ((CompleteVersion) localVersion).resolve(this, false);
			} catch(IOException ioE) {
				throw new RuntimeException("Can't resolve version "+ localVersion, ioE);
			}

		}

		Version remoteVersion = null;

		for (RemoteVersionList list : remoteLists) {
			Version currentVersion = list.getVersion(name);

			if (currentVersion == null)
				continue;

			remoteVersion = currentVersion;
			break;
		}

		return localVersion == null && remoteVersion == null? null : new VersionSyncInfo(localVersion, remoteVersion);
	}

	public LatestVersionSyncInfo getLatestVersionSyncInfo(Version version) {
		if (version == null)
			throw new NullPointerException(
					"Cannot get latest sync info of NULL!");

		VersionSyncInfo syncInfo = getVersionSyncInfo(version);

		return new LatestVersionSyncInfo(version.getReleaseType(), syncInfo);
	}

	public List<VersionSyncInfo> getVersions(VersionFilter filter,
			boolean includeLatest) {
		synchronized (versionFlushLock) {
			return getVersions0(filter, includeLatest);
		}
	}

	public List<VersionSyncInfo> getVersions(boolean includeLatest) {
		return getVersions(TLauncher.getInstance() == null ? null : TLauncher
				.getInstance().getSettings().getVersionFilter(), includeLatest);
	}

	public List<VersionSyncInfo> getVersions() {
		return getVersions(true);
	}

	private List<VersionSyncInfo> getVersions0(VersionFilter filter,
			boolean includeLatest) {
		if (filter == null)
			filter = new VersionFilter();

		List<VersionSyncInfo> plainResult = new ArrayList<VersionSyncInfo>();
		List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();
		Map<String, VersionSyncInfo> lookup = new HashMap<String, VersionSyncInfo>();


		if(includeLatest)
			for (Version version : latestVersions.values()) {
				if (!filter.satisfies(version))
					continue;

				LatestVersionSyncInfo syncInfo = getLatestVersionSyncInfo(version);

				if(!result.contains(syncInfo))
					result.add(syncInfo);
			}

		for (Version version : localList.getVersions()) {
			if (!filter.satisfies(version))
				continue;

			VersionSyncInfo syncInfo = getVersionSyncInfo(version);
			lookup.put(version.getID(), syncInfo);
			plainResult.add(syncInfo);
		}

		for (RemoteVersionList remoteList : remoteLists)
			for (Version version : remoteList.getVersions()) {
				if (lookup.containsKey(version.getID())
						|| !filter.satisfies(version))
					continue;

				VersionSyncInfo syncInfo = getVersionSyncInfo(version);
				lookup.put(version.getID(), syncInfo);
				plainResult.add(syncInfo);
			}

		Collections.sort(plainResult, new Comparator<VersionSyncInfo>() {
			@Override
			public int compare(VersionSyncInfo a, VersionSyncInfo b) {
				Date aDate = a.getLatestVersion().getReleaseTime();
				Date bDate = b.getLatestVersion().getReleaseTime();

				if (aDate == null || bDate == null)
					return 1;

				return bDate.compareTo(aDate);
			}
		});

		result.addAll(plainResult);

		return result;
	}

	public List<VersionSyncInfo> getInstalledVersions(VersionFilter filter) {
		if (filter == null)
			filter = new VersionFilter();

		List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();

		for (Version version : localList.getVersions())
			result.add(getVersionSyncInfo(version));

		return result;
	}

	public List<VersionSyncInfo> getInstalledVersions() {
		return getInstalledVersions(TLauncher.getInstance() == null ? null
				: TLauncher.getInstance().getSettings().getVersionFilter());
	}

	public VersionSyncInfoContainer downloadVersion(VersionSyncInfo syncInfo, boolean force) throws IOException {
		VersionSyncInfoContainer container = new VersionSyncInfoContainer(syncInfo);
		CompleteVersion completeVersion = syncInfo.getCompleteVersion(force);

		File baseDirectory = localList.getBaseDirectory();

		Set<Downloadable> required = syncInfo.getRequiredDownloadables(baseDirectory, force);
		container.addAll(required);

		log("Required for version "+ syncInfo.getID() +':', required);

		String originalId = completeVersion.getJar();

		if(!syncInfo.hasRemote() && originalId == null)
			return container;

		Repository repo;
		String
		id = completeVersion.getID(),
		jarFile = "versions/",
		saveFile = "versions/";

		if(originalId == null) {
			repo = syncInfo.getRemote().getSource();
			jarFile += id + "/" + id + ".jar";
			saveFile = jarFile;
		} else {
			// Original versions should be downloaded from the default repo
			repo = Repository.OFFICIAL_VERSION_REPO;
			jarFile += originalId + "/" + originalId + ".jar";
			saveFile += id + "/" + id + ".jar";
		}

		File file = new File(baseDirectory, saveFile);

		if(!badFile(file))
			return container;

		// Check if file exist, or lookup into original version folder
		if(!force && originalId != null) {
			File
			originalFile = new File(baseDirectory, jarFile),
			originalFileBak = new File(baseDirectory, jarFile +".bak");

			if(originalFile.isFile() && originalFileBak.isFile() && originalFile.length() == originalFileBak.length()) {
				// Copy original version jar into needed version
				FileUtil.copyFile(originalFile, file, true);
				return container;
			}
		}

		Downloadable d = new Downloadable(repo, jarFile, new File(baseDirectory, saveFile), force);
		d.addAdditionalDestination(new File(d.getDestination() + ".bak"));

		log("Jar for "+ syncInfo.getID() +':', d);

		container.add(d);

		return container;
	}

	private static boolean badFile(File file) {
		return !file.isFile() || file.length() == 0L;
	}

	class AsyncRawVersionListObject extends AsyncObject<RawVersionList> {
		private final RemoteVersionList remoteList;

		AsyncRawVersionListObject(RemoteVersionList remoteList) {
			this.remoteList = remoteList;
		}

		RemoteVersionList getVersionList() {
			return remoteList;
		}

		@Override
		protected RawVersionList execute() throws AsyncObjectGotErrorException {
			try {
				return remoteList.getRawList();
			} catch (Exception e) {
				log("Error refreshing version list:", e);
				throw new AsyncObjectGotErrorException(this, e);
			}
		}
	}
}
