package ru.turikhay.tlauncher.managers;

import net.minecraft.launcher.updater.*;
import net.minecraft.launcher.versions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncObject;
import ru.turikhay.util.async.AsyncObjectContainer;
import ru.turikhay.util.async.AsyncObjectGotErrorException;
import ru.turikhay.util.async.AsyncThread;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@ComponentDependence({AssetsManager.class, VersionLists.class, LibraryReplaceProcessor.class})
public class VersionManager extends InterruptibleComponent {
    private static final Logger LOGGER = LogManager.getLogger(VersionManager.class);

    private final LocalVersionList localList;
    private final RemoteVersionList[] remoteLists;
    private Map<ReleaseType, Version> latestVersions;
    private final List<VersionManagerListener> listeners;
    private final Object versionFlushLock, latestVersionsSync;
    private boolean hadRemote;

    public VersionManager(ComponentManager manager) throws Exception {
        super(manager);
        VersionLists list = manager.getComponent(VersionLists.class);
        localList = list.getLocal();
        remoteLists = list.getRemoteLists();
        latestVersions = new LinkedHashMap();
        listeners = Collections.synchronizedList(new ArrayList());
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
        Object var1 = versionFlushLock;
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
            LOGGER.info("Refreshing versions locally...");
        } else {
            LOGGER.info("Refreshing versions remotely...");
            List lock = listeners;
            synchronized (listeners) {
                Iterator e = listeners.iterator();

                while (e.hasNext()) {
                    VersionManagerListener result = (VersionManagerListener) e.next();
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
            LOGGER.info("Version refresh has been cancelled ({} ms)", Time.stop(lock1));
            return false;
        } else {
            Iterator var8;
            List e01;
            VersionManagerListener listener1;
            if (e1 != null) {
                e01 = listeners;
                synchronized (listeners) {
                    var8 = listeners.iterator();

                    while (true) {
                        if (!var8.hasNext()) {
                            break;
                        }

                        listener1 = (VersionManagerListener) var8.next();
                        listener1.onVersionsRefreshingFailed(this);
                    }
                }

                LOGGER.error("Cannot refresh versions ({} ms)", Time.stop(lock1), e1);
                return true;
            } else {
                if (!local) {
                    synchronized (latestVersionsSync) {
                        latestVersions.clear();
                    }
                }

                Map<ReleaseType, Version> latestVersions_ = new LinkedHashMap<ReleaseType, Version>();

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

                LOGGER.info("Versions has been refreshed ({} ms)", Time.stop(lock1));
                refreshList[refreshID] = false;
                e01 = listeners;
                synchronized (listeners) {
                    var8 = listeners.iterator();

                    while (var8.hasNext()) {
                        listener1 = (VersionManagerListener) var8.next();

                        try {
                            listener1.onVersionsRefreshed(this);
                        } catch (Exception e) {
                            LOGGER.warn("Caught listener exception:", e);
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
                LOGGER.warn("Version list {} depends on unavailable {}",
                        object.getVersionList(), checkObject.getVersionList());
                return false;
            }
        }
        return true;
    }

    protected boolean refresh(int queueID) {
        return refresh(queueID, false);
    }

    public void startRefresh(boolean local) {
        refresh(nextID(), local);
    }

    public synchronized void stopRefresh() {
        super.stopRefresh();
        startRefresh(true);
    }

    public void asyncRefresh(final boolean local) {
        AsyncThread.execute(new Runnable() {
            public void run() {
                try {
                    startRefresh(local);
                } catch (Exception var2) {
                    LOGGER.error("Couldn't refresh versions", var2);
                }

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
            AsyncObjectContainer<RawVersionList> container = new AsyncObjectContainer<RawVersionList>();
            RemoteVersionList[] var6 = remoteLists;
            int var5 = remoteLists.length;

            for (int var4 = 0; var4 < var5; ++var4) {
                RemoteVersionList remoteList = var6[var4];
                container.add(new VersionManager.AsyncRawVersionListObject(remoteList));
            }

            return container.execute();
        }
    }

    public void updateVersionList() {
        if (!hadRemote) {
            asyncRefresh();
        } else {
            Iterator var2 = listeners.iterator();

            while (var2.hasNext()) {
                VersionManagerListener listener = (VersionManagerListener) var2.next();
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

    public VersionSyncInfo getVersionSyncInfo(String name) {
        if (name == null) {
            throw new NullPointerException("Cannot get sync info of NULL!");
        } else {
            if (name.startsWith("latest-")) {
                String localVersion = name.substring(7);
                name = null;
                synchronized (latestVersionsSync) {
                    Iterator list = latestVersions.entrySet().iterator();

                    while (list.hasNext()) {
                        Entry remoteVersion = (Entry) list.next();
                        if (remoteVersion.getKey().toString().equals(localVersion)) {
                            name = ((Version) remoteVersion.getValue()).getID();
                            break;
                        }
                    }
                }

                if (name == null) {
                    return null;
                }
            }

            Version var10 = localList.getVersion(name);
            if (var10 instanceof CompleteVersion && ((CompleteVersion) var10).getInheritsFrom() != null) {
                try {
                    var10 = ((CompleteVersion) var10).resolve(this, false);
                } catch (Exception var9) {
                    LOGGER.warn("Can't resolve version {}", var10.getID(), var9);
                    var10 = null;
                }
            }

            Version var11 = null;
            RemoteVersionList[] var7 = remoteLists;
            int var6 = remoteLists.length;

            for (int var5 = 0; var5 < var6; ++var5) {
                RemoteVersionList var12 = var7[var5];
                Version currentVersion = var12.getVersion(name);
                if (currentVersion != null) {
                    var11 = currentVersion;
                    break;
                }
            }

            return var10 == null && var11 == null ? null : new VersionSyncInfo(var10, var11);
        }
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
        Object var3 = versionFlushLock;
        synchronized (versionFlushLock) {
            return getVersions0(filter, includeLatest);
        }
    }

    public List<VersionSyncInfo> getVersions(boolean includeLatest) {
        return getVersions(TLauncher.getInstance() == null ? null : TLauncher.getInstance().getSettings().getVersionFilter(), includeLatest);
    }

    public List<VersionSyncInfo> getVersions() {
        return getVersions(true);
    }

    private List<VersionSyncInfo> getVersions0(VersionFilter filter, boolean includeLatest) {
        if (filter == null) {
            filter = new VersionFilter();
        }

        ArrayList plainResult = new ArrayList();
        ArrayList<VersionSyncInfo> result = new ArrayList<>();
        HashMap lookup = new HashMap();
        Version remoteList;
        Iterator var7;
        if (includeLatest) {
            synchronized (latestVersionsSync) {
                var7 = latestVersions.values().iterator();

                while (var7.hasNext()) {
                    remoteList = (Version) var7.next();
                    if (filter.satisfies(remoteList)) {
                        LatestVersionSyncInfo syncInfo = getLatestVersionSyncInfo(remoteList);
                        if (syncInfo != null && !result.contains(syncInfo)) {
                            result.add(syncInfo);
                        }
                    }
                }
            }
        }

        var7 = localList.getVersions().iterator();

        while (var7.hasNext()) {
            remoteList = (Version) var7.next();
            if (filter.satisfies(remoteList)) {
                VersionSyncInfo var15 = getVersionSyncInfo(remoteList);
                if (var15 != null) {
                    lookup.put(remoteList.getID(), var15);
                    plainResult.add(var15);
                }
            }
        }

        RemoteVersionList[] var9 = remoteLists;
        int var16 = remoteLists.length;

        for (int var14 = 0; var14 < var16; ++var14) {
            RemoteVersionList var13 = var9[var14];
            Iterator var11 = var13.getVersions().iterator();

            while (var11.hasNext()) {
                Version version = (Version) var11.next();
                if (!lookup.containsKey(version.getID()) && filter.satisfies(version)) {
                    VersionSyncInfo syncInfo1 = getVersionSyncInfo(version);
                    if (syncInfo1 != null) {
                        lookup.put(version.getID(), syncInfo1);
                        plainResult.add(syncInfo1);
                    }
                }
            }
        }

        Collections.sort(plainResult, new Comparator<VersionSyncInfo>() {
            public int compare(VersionSyncInfo a, VersionSyncInfo b) {
                Date aDate = a.getLatestVersion().getReleaseTime();
                Date bDate = b.getLatestVersion().getReleaseTime();
                return aDate != null && bDate != null ? bDate.compareTo(aDate) : 1;
            }
        });
        result.addAll(plainResult);

        /*(TLauncher.getInstance() != null && !TLauncher.getInstance().isDebug()) {
            Iterator<VersionSyncInfo> i = result.iterator();
            while (i.hasNext()) {
                VersionSyncInfo vs = i.next();
                if (vs.getLatestVersion().getReleaseType() == ReleaseType.LAUNCHER) {
                    i.remove();
                }
            }
        }*/

        return result;
    }

    public List<VersionSyncInfo> getInstalledVersions(VersionFilter filter) {
        if (filter == null) {
            new VersionFilter();
        }

        ArrayList result = new ArrayList();
        Iterator var4 = localList.getVersions().iterator();

        while (var4.hasNext()) {
            Version version = (Version) var4.next();
            result.add(getVersionSyncInfo(version));
        }

        return result;
    }

    public List<VersionSyncInfo> getInstalledVersions() {
        return getInstalledVersions(TLauncher.getInstance() == null ? null : TLauncher.getInstance().getSettings().getVersionFilter());
    }

    public VersionSyncInfoContainer downloadVersion(VersionSyncInfo syncInfo, Account.AccountType type, boolean force) throws IOException {
        Rule.FeatureMatcher featureMatcher = new CurrentLaunchFeatureMatcher();
        VersionSyncInfoContainer container = new VersionSyncInfoContainer(syncInfo);

        CompleteVersion completeVersion = syncInfo.resolveCompleteVersion(this, false);
        /*if (syncInfo.getLocal() == completeVersion) {
            syncInfo.setLocal(completeVersion);
        } else {
            syncInfo.setRemote(completeVersion);
        }*/

        if (type != null) {
            completeVersion = manager.getComponent(LibraryReplaceProcessor.class).process(completeVersion, type);
        }

        File baseDirectory = localList.getBaseDirectory();

        try {
            container.addAll(syncInfo.getRequiredDownloadables(featureMatcher, baseDirectory, force, type == null? Account.AccountType.PLAIN : type));
        } catch (IOException ioE) {
            LOGGER.warn("Could not fetch required downloads for {}", syncInfo.getID(), ioE);
        }

        LOGGER.debug("Required for version " + syncInfo.getID() + ':', container.getList());

        File destination = new File(baseDirectory, "versions/" + completeVersion.getID() + "/" + completeVersion.getID() + ".jar");

        if (!force && destination.isFile() && destination.length() > 0L) {
            return container;
        }

        VersionDownloadable jarDownloadable = new VersionDownloadable(completeVersion, destination, syncInfo.getRemote() != null ? syncInfo.getRemote().getSource() : null);

        LOGGER.debug("Jar for {}: {}", syncInfo.getID(), jarDownloadable);

        container.add(jarDownloadable);
        return container;
    }

    private class AsyncRawVersionListObject extends AsyncObject<RawVersionList> {
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
                LOGGER.error("Error refreshing {}", remoteList, var2);
                throw new AsyncObjectGotErrorException(this, var2);
            }
        }
    }
}
