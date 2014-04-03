package com.turikhay.tlauncher.managers;

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

import net.minecraft.launcher.updater.LatestVersionSyncInfo;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionList.RawVersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.updater.VersionFilter;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.ComponentDependence;
import com.turikhay.tlauncher.component.InterruptibleComponent;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.repository.Repository;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncObject;
import com.turikhay.util.async.AsyncObjectContainer;
import com.turikhay.util.async.AsyncThread;

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

		latestVersions.clear();

		for (VersionManagerListener listener : listeners)
			listener.onVersionsRefreshing(this);

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
			log("Version refresh has been cancelled (" + Time.stop(lock)
					+ " ms)");
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

	void asyncRefresh(final boolean local) {
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

	private Map<AsyncObject<RawVersionList>, RawVersionList> refreshVersions(
			boolean local) throws IOException {
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
		Version remoteVersion = null;

		for (RemoteVersionList list : remoteLists) {
			Version currentVersion = list.getVersion(name);
			if (currentVersion == null)
				continue;

			remoteVersion = currentVersion;
			break;
		}

		// TODO may be return null if all versions are null?
		return new VersionSyncInfo(localVersion, remoteVersion);
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

		for (Version version : latestVersions.values()) {
			if (!filter.satisfies(version))
				continue;

			LatestVersionSyncInfo syncInfo = getLatestVersionSyncInfo(version);
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

	public DownloadableContainer downloadVersion(VersionSyncInfo syncInfo,
			boolean force) throws IOException {
		DownloadableContainer container = new DownloadableContainer();

		CompleteVersion version = syncInfo.getCompleteVersion(force);
		File baseDirectory = localList.getBaseDirectory();

		log("Required downloadables:",
				syncInfo.getRequiredDownloadables(baseDirectory, force));

		container.addAll(syncInfo
				.getRequiredDownloadables(baseDirectory, force));

		if (!syncInfo.hasRemote())
			return container;

		Repository repo;
		String id = version.getID(), o_id = version.getOriginal(), jarFile = "versions/", saveFile = jarFile;

		if (o_id != null) {
			// Original versions should be downloaded from the default repo
			repo = Repository.OFFICIAL_VERSION_REPO;
			jarFile += o_id + "/" + o_id + ".jar";
			saveFile += id + "/" + id + ".jar";
		} else {
			repo = syncInfo.getRemote().getSource();
			jarFile += id + "/" + id + ".jar";
			saveFile = jarFile;
		}

		File file = new File(baseDirectory, saveFile);
		if (!force && file.isFile() && file.length() > 0)
			return container;

		Downloadable d = new Downloadable(repo, jarFile, new File(
				baseDirectory, saveFile), force);
		d.addAdditionalDestination(new File(d.getDestination() + ".bak"));

		container.add(d);

		return container;
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
		protected RawVersionList execute() {
			try {
				return remoteList.getRawList();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
