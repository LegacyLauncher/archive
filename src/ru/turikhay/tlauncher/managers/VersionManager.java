package ru.turikhay.tlauncher.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.launcher.updater.LatestVersionSyncInfo;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncObject;
import ru.turikhay.util.async.AsyncObjectContainer;
import ru.turikhay.util.async.AsyncThread;

@ComponentDependence({AssetsManager.class, VersionLists.class})
public class VersionManager extends InterruptibleComponent {
   private final LocalVersionList localList;
   private final RemoteVersionList[] remoteLists;
   private Map latestVersions;
   private final List listeners;
   private final Object versionFlushLock;

   public VersionManager(ComponentManager manager) throws Exception {
      super(manager);
      VersionLists list = (VersionLists)manager.getComponent(VersionLists.class);
      this.localList = list.getLocal();
      this.remoteLists = list.getRemoteLists();
      this.latestVersions = new LinkedHashMap();
      this.listeners = Collections.synchronizedList(new ArrayList());
      this.versionFlushLock = new Object();
   }

   public void addListener(VersionManagerListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         this.listeners.add(listener);
      }
   }

   public LocalVersionList getLocalList() {
      return this.localList;
   }

   public Map getLatestVersions() {
      synchronized(this.versionFlushLock) {
         return Collections.unmodifiableMap(this.latestVersions);
      }
   }

   boolean refresh(int refreshID, boolean local) {
      this.refreshList[refreshID] = true;
      this.log(new Object[]{"Refreshing versions..."});
      if (!local) {
         this.latestVersions.clear();
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            VersionManagerListener listener = (VersionManagerListener)var4.next();
            listener.onVersionsRefreshing(this);
         }
      }

      Object lock = new Object();
      Time.start(lock);
      Map result = null;
      Throwable e = null;

      try {
         result = this.refreshVersions(local);
      } catch (Throwable var12) {
         e = var12;
      }

      if (this.isCancelled(refreshID)) {
         this.log(new Object[]{"Version refresh has been cancelled (" + Time.stop(lock) + " ms)"});
         return false;
      } else {
         VersionManagerListener listener;
         Iterator var16;
         if (e != null) {
            var16 = this.listeners.iterator();

            while(var16.hasNext()) {
               listener = (VersionManagerListener)var16.next();
               listener.onVersionsRefreshingFailed(this);
            }

            this.log(new Object[]{"Cannot refresh versions (" + Time.stop(lock) + " ms)", e});
            return true;
         } else {
            if (result != null) {
               synchronized(this.versionFlushLock) {
                  Iterator var8 = result.keySet().iterator();

                  while(var8.hasNext()) {
                     AsyncObject object = (AsyncObject)var8.next();
                     VersionList.RawVersionList rawList = (VersionList.RawVersionList)result.get(object);
                     if (rawList != null) {
                        VersionManager.AsyncRawVersionListObject listObject = (VersionManager.AsyncRawVersionListObject)object;
                        RemoteVersionList versionList = listObject.getVersionList();
                        versionList.refreshVersions(rawList);
                        this.latestVersions.putAll(versionList.getLatestVersions());
                     }
                  }
               }
            }

            this.latestVersions = U.sortMap(this.latestVersions, ReleaseType.values());
            this.log(new Object[]{"Versions has been refreshed (" + Time.stop(lock) + " ms)"});
            this.refreshList[refreshID] = false;
            var16 = this.listeners.iterator();

            while(var16.hasNext()) {
               listener = (VersionManagerListener)var16.next();
               listener.onVersionsRefreshed(this);
            }

            return true;
         }
      }
   }

   protected boolean refresh(int queueID) {
      return this.refresh(queueID, false);
   }

   public void startRefresh(boolean local) {
      this.refresh(this.nextID(), local);
   }

   public synchronized void stopRefresh() {
      super.stopRefresh();
      this.startRefresh(true);
   }

   void asyncRefresh(final boolean local) {
      AsyncThread.execute(new Runnable() {
         public void run() {
            VersionManager.this.startRefresh(local);
         }
      });
   }

   public void asyncRefresh() {
      this.asyncRefresh(false);
   }

   private Map refreshVersions(boolean local) throws IOException {
      this.localList.refreshVersions();
      if (local) {
         return null;
      } else {
         AsyncObjectContainer container = new AsyncObjectContainer();
         RemoteVersionList[] var6;
         int var5 = (var6 = this.remoteLists).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            RemoteVersionList remoteList = var6[var4];
            container.add(new VersionManager.AsyncRawVersionListObject(remoteList));
         }

         return container.execute();
      }
   }

   public void updateVersionList() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         VersionManagerListener listener = (VersionManagerListener)var2.next();
         listener.onVersionsRefreshed(this);
      }

   }

   public VersionSyncInfo getVersionSyncInfo(Version version) {
      return this.getVersionSyncInfo(version.getID());
   }

   public VersionSyncInfo getVersionSyncInfo(String name) {
      if (name == null) {
         throw new NullPointerException("Cannot get sync info of NULL!");
      } else {
         if (name.startsWith("latest-")) {
            String realID = name.substring(7);
            name = null;
            Iterator var4 = this.latestVersions.entrySet().iterator();

            while(var4.hasNext()) {
               Entry entry = (Entry)var4.next();
               if (((ReleaseType)entry.getKey()).toString().equals(realID)) {
                  name = ((Version)entry.getValue()).getID();
                  break;
               }
            }

            if (name == null) {
               return null;
            }
         }

         Version localVersion = this.localList.getVersion(name);
         Version remoteVersion = null;
         RemoteVersionList[] var7;
         int var6 = (var7 = this.remoteLists).length;

         for(int var5 = 0; var5 < var6; ++var5) {
            RemoteVersionList list = var7[var5];
            Version currentVersion = list.getVersion(name);
            if (currentVersion != null) {
               remoteVersion = currentVersion;
               break;
            }
         }

         return new VersionSyncInfo(localVersion, remoteVersion);
      }
   }

   public LatestVersionSyncInfo getLatestVersionSyncInfo(Version version) {
      if (version == null) {
         throw new NullPointerException("Cannot get latest sync info of NULL!");
      } else {
         VersionSyncInfo syncInfo = this.getVersionSyncInfo(version);
         return new LatestVersionSyncInfo(version.getReleaseType(), syncInfo);
      }
   }

   public List getVersions(VersionFilter filter, boolean includeLatest) {
      synchronized(this.versionFlushLock) {
         return this.getVersions0(filter, includeLatest);
      }
   }

   public List getVersions(boolean includeLatest) {
      return this.getVersions(TLauncher.getInstance() == null ? null : TLauncher.getInstance().getSettings().getVersionFilter(), includeLatest);
   }

   public List getVersions() {
      return this.getVersions(true);
   }

   private List getVersions0(VersionFilter filter, boolean includeLatest) {
      if (filter == null) {
         filter = new VersionFilter();
      }

      List plainResult = new ArrayList();
      List result = new ArrayList();
      Map lookup = new HashMap();
      Version version;
      Iterator var7;
      if (includeLatest) {
         var7 = this.latestVersions.values().iterator();

         while(var7.hasNext()) {
            version = (Version)var7.next();
            if (filter.satisfies(version)) {
               LatestVersionSyncInfo syncInfo = this.getLatestVersionSyncInfo(version);
               result.add(syncInfo);
            }
         }
      }

      var7 = this.localList.getVersions().iterator();

      while(var7.hasNext()) {
         version = (Version)var7.next();
         if (filter.satisfies(version)) {
            VersionSyncInfo syncInfo = this.getVersionSyncInfo(version);
            lookup.put(version.getID(), syncInfo);
            plainResult.add(syncInfo);
         }
      }

      RemoteVersionList[] var9;
      int var16 = (var9 = this.remoteLists).length;

      for(int var14 = 0; var14 < var16; ++var14) {
         RemoteVersionList remoteList = var9[var14];
         Iterator var11 = remoteList.getVersions().iterator();

         while(var11.hasNext()) {
            Version version = (Version)var11.next();
            if (!lookup.containsKey(version.getID()) && filter.satisfies(version)) {
               VersionSyncInfo syncInfo = this.getVersionSyncInfo(version);
               lookup.put(version.getID(), syncInfo);
               plainResult.add(syncInfo);
            }
         }
      }

      Collections.sort(plainResult, new Comparator() {
         public int compare(VersionSyncInfo a, VersionSyncInfo b) {
            Date aDate = a.getLatestVersion().getReleaseTime();
            Date bDate = b.getLatestVersion().getReleaseTime();
            return aDate != null && bDate != null ? bDate.compareTo(aDate) : 1;
         }
      });
      result.addAll(plainResult);
      return result;
   }

   public List getInstalledVersions(VersionFilter filter) {
      if (filter == null) {
         new VersionFilter();
      }

      List result = new ArrayList();
      Iterator var4 = this.localList.getVersions().iterator();

      while(var4.hasNext()) {
         Version version = (Version)var4.next();
         result.add(this.getVersionSyncInfo(version));
      }

      return result;
   }

   public List getInstalledVersions() {
      return this.getInstalledVersions(TLauncher.getInstance() == null ? null : TLauncher.getInstance().getSettings().getVersionFilter());
   }

   public VersionSyncInfoContainer downloadVersion(VersionSyncInfo syncInfo, boolean force) throws IOException {
      VersionSyncInfoContainer container = new VersionSyncInfoContainer(syncInfo);
      CompleteVersion version = syncInfo.getCompleteVersion(force);
      File baseDirectory = this.localList.getBaseDirectory();
      Set required = syncInfo.getRequiredDownloadables(baseDirectory, force);
      this.log(new Object[]{"Required for " + syncInfo.getID() + ":", required});
      container.addAll(syncInfo.getRequiredDownloadables(baseDirectory, force));
      if (!syncInfo.hasRemote()) {
         return container;
      } else {
         String id = version.getID();
         String o_id = version.getOriginal();
         String jarFile = "versions/";
         String saveFile = jarFile;
         Repository repo;
         if (o_id != null) {
            repo = Repository.OFFICIAL_VERSION_REPO;
            jarFile = jarFile + o_id + "/" + o_id + ".jar";
            saveFile = saveFile + id + "/" + id + ".jar";
         } else {
            repo = syncInfo.getRemote().getSource();
            jarFile = jarFile + id + "/" + id + ".jar";
            saveFile = jarFile;
         }

         File file = new File(baseDirectory, saveFile);
         if (!force && file.isFile() && file.length() > 0L) {
            return container;
         } else {
            Downloadable d = new Downloadable(repo, jarFile, new File(baseDirectory, saveFile), force);
            d.addAdditionalDestination(new File(d.getDestination() + ".bak"));
            container.add(d);
            return container;
         }
      }
   }

   class AsyncRawVersionListObject extends AsyncObject {
      private final RemoteVersionList remoteList;

      AsyncRawVersionListObject(RemoteVersionList remoteList) {
         this.remoteList = remoteList;
      }

      RemoteVersionList getVersionList() {
         return this.remoteList;
      }

      protected VersionList.RawVersionList execute() {
         try {
            return this.remoteList.getRawList();
         } catch (IOException var2) {
            throw new RuntimeException(var2);
         }
      }
   }
}
