package com.turikhay.tlauncher.component.managers;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.ComponentDependence;
import com.turikhay.tlauncher.component.InterruptibleComponent;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.repository.Repository;
import com.turikhay.util.Time;
import com.turikhay.util.async.AsyncObject;
import com.turikhay.util.async.AsyncObjectContainer;
import com.turikhay.util.async.AsyncThread;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;

@ComponentDependence({AssetsManager.class, VersionLists.class})
public class VersionManager extends InterruptibleComponent {
   private final LocalVersionList localList;
   private final RemoteVersionList[] remoteLists;
   private final List listeners;
   private final Object versionFlushLock;

   public VersionManager(ComponentManager manager) throws Exception {
      super(manager);
      VersionLists list = (VersionLists)manager.getComponent(VersionLists.class);
      this.localList = list.getLocal();
      this.remoteLists = list.getRemoteLists();
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

   protected boolean refresh(int refreshID, boolean local) {
      this.refreshList[refreshID] = true;
      this.log(new Object[]{"Refreshing versions..."});
      Iterator var4 = this.listeners.iterator();

      while(var4.hasNext()) {
         VersionManagerListener listener = (VersionManagerListener)var4.next();
         listener.onVersionsRefreshing(this);
      }

      Object lock = new Object();
      Time.start(lock);
      Map result = null;
      Throwable e = null;

      try {
         result = this.refreshVersions(local);
      } catch (Throwable var11) {
         e = var11;
      }

      if (this.isCancelled(refreshID)) {
         this.log(new Object[]{"Version refresh has been cancelled (" + Time.stop(lock) + " ms)"});
         return false;
      } else {
         VersionManagerListener listener;
         Iterator var15;
         if (e != null) {
            var15 = this.listeners.iterator();

            while(var15.hasNext()) {
               listener = (VersionManagerListener)var15.next();
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
                        listObject.getVersionList().refreshVersions(rawList);
                     }
                  }
               }
            }

            this.log(new Object[]{"Versions has been refreshed (" + Time.stop(lock) + " ms)"});
            this.refreshList[refreshID] = false;
            var15 = this.listeners.iterator();

            while(var15.hasNext()) {
               listener = (VersionManagerListener)var15.next();
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

   public void asyncRefresh(final boolean local) {
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

   public List getVersions(VersionFilter filter) {
      synchronized(this.versionFlushLock) {
         return this.getVersions0(filter);
      }
   }

   public List getVersions() {
      return this.getVersions(TLauncher.getInstance() == null ? null : TLauncher.getInstance().getSettings().getVersionFilter());
   }

   private List getVersions0(VersionFilter filter) {
      if (filter == null) {
         filter = new VersionFilter();
      }

      List result = new ArrayList();
      Map lookup = new HashMap();
      Iterator var5 = this.localList.getVersions().iterator();

      while(var5.hasNext()) {
         Version version = (Version)var5.next();
         if (filter.satisfies(version)) {
            VersionSyncInfo syncInfo = this.getVersionSyncInfo(version);
            lookup.put(version.getID(), syncInfo);
            result.add(syncInfo);
         }
      }

      RemoteVersionList[] var7;
      int var13 = (var7 = this.remoteLists).length;

      for(int var12 = 0; var12 < var13; ++var12) {
         RemoteVersionList remoteList = var7[var12];
         Iterator var9 = remoteList.getVersions().iterator();

         while(var9.hasNext()) {
            Version version = (Version)var9.next();
            if (!lookup.containsKey(version.getID()) && filter.satisfies(version)) {
               VersionSyncInfo syncInfo = this.getVersionSyncInfo(version);
               lookup.put(version.getID(), syncInfo);
               result.add(syncInfo);
            }
         }
      }

      Collections.sort(result, new Comparator() {
         public int compare(VersionSyncInfo a, VersionSyncInfo b) {
            Version aVer = a.getLatestVersion();
            Version bVer = b.getLatestVersion();
            return aVer.getReleaseTime() != null && bVer.getReleaseTime() != null ? bVer.getReleaseTime().compareTo(aVer.getReleaseTime()) : bVer.getUpdatedTime().compareTo(aVer.getUpdatedTime());
         }
      });
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

   public DownloadableContainer downloadVersion(VersionSyncInfo syncInfo, boolean force) throws IOException {
      DownloadableContainer container = new DownloadableContainer();
      if (!syncInfo.hasRemote()) {
         return container;
      } else {
         CompleteVersion version = syncInfo.getCompleteVersion(force);
         File baseDirectory = this.localList.getBaseDirectory();
         container.addAll((Collection)syncInfo.getRequiredDownloadables(baseDirectory, force));
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

      protected RemoteVersionList getVersionList() {
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
