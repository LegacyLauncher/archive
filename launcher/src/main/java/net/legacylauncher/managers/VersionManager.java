package net.legacylauncher.managers;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.component.ComponentDependence;
import net.legacylauncher.component.InterruptibleComponent;
import net.legacylauncher.util.Time;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.AsyncObject;
import net.legacylauncher.util.async.AsyncObjectContainer;
import net.legacylauncher.util.async.AsyncObjectGotErrorException;
import net.legacylauncher.util.async.AsyncThread;
import net.minecraft.launcher.updater.*;
import net.minecraft.launcher.versions.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
@ComponentDependence({AssetsManager.class, VersionLists.class, LibraryReplaceProcessor.class})
public class VersionManager extends InterruptibleComponent {
    private final LocalVersionList localList;
    private final RemoteVersionList[] remoteLists;
    private Map<ReleaseType, Version> latestVersions;
    private final List<VersionManagerListener> listeners;
    private final Object versionFlushLock, latestVersionsSync;
    private boolean hadRemote;

    public VersionManager(ComponentManager manager) {
        super(manager);
        VersionLists list = manager.getComponent(VersionLists.class);
        localList = list.getLocal();
        remoteLists = list.getRemoteLists();
        latestVersions = new LinkedHashMap<>();
        listeners = Collections.synchronizedList(new ArrayList<>());
        versionFlushLock = new Object();
        latestVersionsSync = new Object();
    }

    public void addListener(VersionManagerListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            listeners.add(listener);
        }
    }

    public LocalVersionList getLocalList() {
        return localList;
    }

    public Map<ReleaseType, Version> getLatestVersions() {
        synchronized (versionFlushLock) {
            synchronized (latestVersionsSync) {
                return Collections.unmodifiableMap(new HashMap<>(latestVersions));
            }
        }
    }

    boolean refresh(int refreshID, boolean local) {
        refreshList[refreshID] = true;
        local |= !manager.getLauncher().getSettings().getBoolean("minecraft.versions.sub.remote");
        hadRemote |= !local;
        if (local) {
            log.info("Refreshing versions locally...");
        } else {
            log.info("Refreshing versions remotely...");
            synchronized (listeners) {

                for (VersionManagerListener result : listeners) {
                    result.onVersionsRefreshing(this);
                }
            }
        }

        Object lock1 = new Object();
        Time.start(lock1);
        Map<AsyncObject<RawVersionList>, RawVersionList> result1 = null;
        Throwable e1 = null;

        try {
            result1 = refreshVersions(local);
        } catch (Throwable var12) {
            e1 = var12;
        }

        if (isCancelled(refreshID)) {
            log.info("Version refresh has been cancelled ({} ms)", Time.stop(lock1));
            return false;
        } else {
            if (e1 != null) {
                synchronized (listeners) {
                    for (VersionManagerListener listener : listeners) {
                        listener.onVersionsRefreshingFailed(this);
                    }
                }

                log.error("Cannot refresh versions ({} ms)", Time.stop(lock1), e1);
                return true;
            } else {
                if (!local) {
                    synchronized (latestVersionsSync) {
                        latestVersions.clear();
                    }
                }

                Map<ReleaseType, Version> latestVersions_ = new LinkedHashMap<>();

                if (result1 != null) {
                    synchronized (versionFlushLock) {
                        Set<Map.Entry<AsyncObject<RawVersionList>, RawVersionList>> entrySet = result1.entrySet();
                        for (Entry<AsyncObject<RawVersionList>, RawVersionList> entry : entrySet) {
                            if (checkConsistency(entrySet, entry)) {
                                AsyncRawVersionListObject object = (AsyncRawVersionListObject) entry.getKey();
                                RawVersionList list = entry.getValue();
                                object.getVersionList().refreshVersions(list);
                                latestVersions_.putAll(object.getVersionList().getLatestVersions());
                            }
                        }
                    }
                }

                synchronized (latestVersionsSync) {
                    latestVersions.putAll(latestVersions_);
                    latestVersions = U.sortMap(latestVersions, ReleaseType.values());
                }

                log.info("Versions has been refreshed ({} ms)", Time.stop(lock1));
                refreshList[refreshID] = false;
                synchronized (listeners) {
                    for (VersionManagerListener listener : listeners) {
                        try {
                            listener.onVersionsRefreshed(this);
                        } catch (Exception e) {
                            log.warn("Caught listener exception:", e);
                        }
                    }

                    return true;
                }
            }
        }
    }

    private boolean checkConsistency(Set<Map.Entry<AsyncObject<RawVersionList>, RawVersionList>> set,
                                     Map.Entry<AsyncObject<RawVersionList>, RawVersionList> entry) {
        RawVersionList list = entry.getValue();
        if (list == null) {
            return false;
        }

        AsyncRawVersionListObject object = (AsyncRawVersionListObject) entry.getKey();
        if (object.getVersionList().getDependencies().isEmpty()) {
            return true;
        }

        for (Map.Entry<AsyncObject<RawVersionList>, RawVersionList> checkEntry : set) {
            if (checkEntry.getValue() == list && checkEntry.getKey() == object) {
                continue;
            }
            AsyncRawVersionListObject checkObject = (AsyncRawVersionListObject) checkEntry.getKey();
            if (object.getVersionList().getDependencies().contains(checkObject.getVersionList())) {
                if (checkConsistency(set, checkEntry)) {
                    continue;
                }
                log.warn("Version list {} depends on unavailable {}",
                        object.getVersionList(), checkObject.getVersionList());
                return false;
            }
        }
        return true;
    }

    protected boolean refresh(int refreshID) {
        return refresh(refreshID, false);
    }

    public void startRefresh(boolean local) {
        refresh(nextID(), local);
    }

    public synchronized void stopRefresh() {
        super.stopRefresh();
        startRefresh(true);
    }

    public void asyncRefresh(final boolean local) {
        AsyncThread.execute(() -> {
            try {
                startRefresh(local);
            } catch (Exception var2) {
                log.error("Couldn't refresh versions", var2);
            }
        });
    }

    public void asyncRefresh() {
        asyncRefresh(false);
    }

    private Map<AsyncObject<RawVersionList>, RawVersionList> refreshVersions(boolean local) throws IOException {
        localList.refreshVersions();
        if (local) {
            return null;
        } else {
            AsyncObjectContainer<RawVersionList> container = new AsyncObjectContainer<>();

            for (RemoteVersionList remoteList : remoteLists) {
                container.add(new AsyncRawVersionListObject(remoteList));
            }

            return container.execute();
        }
    }

    public void updateVersionList() {
        if (!hadRemote) {
            asyncRefresh();
        } else {
            for (VersionManagerListener listener : listeners) {
                listener.onVersionsRefreshed(this);
            }
        }

    }

    public VersionSyncInfo getVersionSyncInfo(Version version) {
        if (version == null) {
            throw new NullPointerException();
        } else {
            return getVersionSyncInfo(version.getID());
        }
    }

    public VersionSyncInfo getVersionSyncInfo(String name, List<String> inheritance) {
        if (name == null) {
            throw new NullPointerException("Cannot get sync info of NULL!");
        } else {
            if (name.startsWith("latest-")) {
                String localVersion = name.substring(7);
                name = null;
                synchronized (latestVersionsSync) {

                    for (Entry<ReleaseType, Version> remoteVersion : latestVersions.entrySet()) {
                        if (remoteVersion.getKey().toString().equals(localVersion)) {
                            name = remoteVersion.getValue().getID();
                            break;
                        }
                    }
                }

                if (name == null) {
                    return null;
                }
            }

            Version localVersion = localList.getVersion(name);
            if (localVersion instanceof CompleteVersion && ((CompleteVersion) localVersion).getInheritsFrom() != null) {
                if (inheritance == null) {
                    inheritance = new ArrayList<>();
                }
                try {
                    localVersion = ((CompleteVersion) localVersion).resolve(this, false, inheritance);
                } catch (Exception e) {
                    log.warn("Can't resolve version {}", localVersion.getID(), e);
                    localVersion = null;
                }
            }

            String finalName = name;
            Optional<Version> remoteVersion = Arrays.stream(remoteLists)
                    .map(list -> list.getVersion(finalName))
                    .filter(Objects::nonNull)
                    .findFirst();

            return localVersion == null && !remoteVersion.isPresent() ? null : new VersionSyncInfo(localVersion, remoteVersion.orElse(null));
        }
    }

    public VersionSyncInfo getVersionSyncInfo(String name) {
        return getVersionSyncInfo(name, null);
    }

    public LatestVersionSyncInfo getLatestVersionSyncInfo(Version version) {
        if (version == null) {
            throw new NullPointerException("Cannot get latest sync info of NULL!");
        } else {
            VersionSyncInfo syncInfo = getVersionSyncInfo(version);
            return new LatestVersionSyncInfo(version.getReleaseType(), syncInfo);
        }
    }

    public List<VersionSyncInfo> getVersions(VersionFilter filter, boolean includeLatest) {
        synchronized (versionFlushLock) {
            return getVersions0(filter, includeLatest);
        }
    }

    public List<VersionSyncInfo> getVersions(boolean includeLatest) {
        return getVersions(LegacyLauncher.getInstance() == null ? null : LegacyLauncher.getInstance().getSettings().getVersionFilter(), includeLatest);
    }

    private List<VersionSyncInfo> getVersions0(VersionFilter filter, boolean includeLatest) {
        if (filter == null) {
            filter = new VersionFilter();
        }

        ArrayList<VersionSyncInfo> plainResult = new ArrayList<>();
        ArrayList<VersionSyncInfo> result = new ArrayList<>();
        Map<String, VersionSyncInfo> lookup = new HashMap<>();
        Version remoteList;
        if (includeLatest) {
            synchronized (latestVersionsSync) {

                for (Version version : latestVersions.values()) {
                    remoteList = version;
                    if (filter.satisfies(remoteList)) {
                        LatestVersionSyncInfo syncInfo = getLatestVersionSyncInfo(remoteList);
                        if (syncInfo != null && !result.contains(syncInfo)) {
                            result.add(syncInfo);
                        }
                    }
                }
            }
        }

        for (Version value : localList.getVersions()) {
            remoteList = value;
            if (filter.satisfies(remoteList)) {
                VersionSyncInfo var15 = getVersionSyncInfo(remoteList);
                if (var15 != null) {
                    lookup.put(remoteList.getID(), var15);
                    plainResult.add(var15);
                }
            }
        }

        for (RemoteVersionList var13 : remoteLists) {

            for (Version version : var13.getVersions()) {
                if (!lookup.containsKey(version.getID()) && filter.satisfies(version)) {
                    VersionSyncInfo syncInfo1 = getVersionSyncInfo(version);
                    if (syncInfo1 != null) {
                        lookup.put(version.getID(), syncInfo1);
                        plainResult.add(syncInfo1);
                    }
                }
            }
        }

        plainResult.sort((a, b) -> {
            Date aDate = a.getLatestVersion().getReleaseTime();
            Date bDate = b.getLatestVersion().getReleaseTime();
            if (aDate == null && bDate == null) {
                return 0;
            } else if (aDate == null) {
                return 1;
            } else if (bDate == null) {
                return -1;
            } else {
                return bDate.compareTo(aDate);
            }
        });
        result.addAll(plainResult);

        return result;
    }

    public List<VersionSyncInfo> getInstalledVersions(VersionFilter filter) {
        if (filter == null) {
            new VersionFilter();
        }

        List<VersionSyncInfo> result = new ArrayList<>();

        for (Version version : localList.getVersions()) {
            result.add(getVersionSyncInfo(version));
        }

        return result;
    }

    public List<VersionSyncInfo> getInstalledVersions() {
        return getInstalledVersions(LegacyLauncher.getInstance() == null ? null : LegacyLauncher.getInstance().getSettings().getVersionFilter());
    }

    public VersionSyncInfoContainer downloadVersion(VersionSyncInfo syncInfo, @Nonnull String[] types, boolean force) throws IOException {
        Rule.FeatureMatcher featureMatcher = new CurrentLaunchFeatureMatcher();
        VersionSyncInfoContainer container = new VersionSyncInfoContainer(syncInfo);

        CompleteVersion completeVersion = syncInfo.resolveCompleteVersion(this, force);
        /*if (syncInfo.getLocal() == completeVersion) {
            syncInfo.setLocal(completeVersion);
        } else {
            syncInfo.setRemote(completeVersion);
        }*/

        if (types.length > 0) {
            completeVersion = manager.getComponent(LibraryReplaceProcessor.class).process(completeVersion, types);
        }

        File baseDirectory = localList.getBaseDirectory();

        try {
            container.addAll(syncInfo.getRequiredDownloadables(featureMatcher, baseDirectory, force, types));
        } catch (IOException ioE) {
            log.warn("Could not fetch required downloads for {}", syncInfo.getID(), ioE);
        }

        log.debug("Required for version " + syncInfo.getID() + ':', container.getList());

        File destination = new File(baseDirectory, "versions/" + completeVersion.getID() + "/" + completeVersion.getID() + ".jar");

        if (!force && destination.isFile() && destination.length() > 0L) {
            return container;
        }

        VersionDownloadable jarDownloadable = new VersionDownloadable(completeVersion, destination, syncInfo.getRemote() != null ? syncInfo.getRemote().getSource() : null);

        log.debug("Jar for {}: {}", syncInfo.getID(), jarDownloadable);

        container.add(jarDownloadable);
        return container;
    }

    private static class AsyncRawVersionListObject extends AsyncObject<RawVersionList> {
        private final RemoteVersionList remoteList;

        AsyncRawVersionListObject(RemoteVersionList remoteList) {
            this.remoteList = remoteList;
        }

        RemoteVersionList getVersionList() {
            return remoteList;
        }

        protected RawVersionList execute() throws AsyncObjectGotErrorException {
            try {
                return remoteList.getRawList();
            } catch (Exception var2) {
                log.error("Error refreshing {}", remoteList, var2);
                throw new AsyncObjectGotErrorException(this, var2);
            }
        }
    }
}
