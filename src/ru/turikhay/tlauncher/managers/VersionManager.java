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
import net.minecraft.launcher.updater.VersionDownloadable;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncObject;
import ru.turikhay.util.async.AsyncObjectContainer;
import ru.turikhay.util.async.AsyncObjectGotErrorException;
import ru.turikhay.util.async.AsyncThread;

@ComponentDependence({AssetsManager.class, VersionLists.class, ElyManager.class})
public class VersionManager extends InterruptibleComponent {
   private final LocalVersionList localList;
   private final RemoteVersionList[] remoteLists;
   private Map latestVersions;
   private final List listeners;
   private final Object versionFlushLock;
   private boolean hadRemote;

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

   boolean refresh(int refreshID, boolean local) {
      this.refreshList[refreshID] = true;
      local |= !this.manager.getLauncher().getSettings().getBoolean("minecraft.versions.sub.remote");
      this.hadRemote |= !local;
      if (local) {
         this.log(new Object[]{"Refreshing versions locally..."});
      } else {
         this.log(new Object[]{"Refreshing versions remotely..."});
         List lock = this.listeners;
         synchronized(this.listeners) {
            Iterator e = this.listeners.iterator();

            while(e.hasNext()) {
               VersionManagerListener result = (VersionManagerListener)e.next();
               result.onVersionsRefreshing(this);
            }
         }
      }

      Object lock1 = new Object();
      Time.start(lock1);
      Map result1 = null;
      Throwable e1 = null;

      try {
         result1 = this.refreshVersions(local);
      } catch (Throwable var19) {
         e1 = var19;
      }

      if (this.isCancelled(refreshID)) {
         this.log(new Object[]{"Version refresh has been cancelled (" + Time.stop(lock1) + " ms)"});
         return false;
      } else {
         List e01;
         VersionManagerListener listener1;
         Iterator var8;
         if (e1 != null) {
            e01 = this.listeners;
            synchronized(this.listeners) {
               var8 = this.listeners.iterator();

               while(true) {
                  if (!var8.hasNext()) {
                     break;
                  }

                  listener1 = (VersionManagerListener)var8.next();
                  listener1.onVersionsRefreshingFailed(this);
               }
            }

            this.log(new Object[]{"Cannot refresh versions (" + Time.stop(lock1) + " ms)", e1});
            return true;
         } else {
            if (!local) {
               this.latestVersions.clear();
            }

            Map latestVersions_ = new LinkedHashMap();
            if (result1 != null) {
               Object e0 = this.versionFlushLock;
               synchronized(this.versionFlushLock) {
                  var8 = result1.keySet().iterator();

                  while(var8.hasNext()) {
                     AsyncObject listener = (AsyncObject)var8.next();
                     VersionList.RawVersionList rawList = (VersionList.RawVersionList)result1.get(listener);
                     if (rawList != null) {
                        VersionManager.AsyncRawVersionListObject listObject = (VersionManager.AsyncRawVersionListObject)listener;
                        RemoteVersionList versionList = listObject.getVersionList();
                        versionList.refreshVersions(rawList);
                        latestVersions_.putAll(versionList.getLatestVersions());
                     }
                  }
               }
            }

            this.latestVersions.putAll(latestVersions_);
            this.latestVersions = U.sortMap(this.latestVersions, ReleaseType.values());
            this.log(new Object[]{"Versions has been refreshed (" + Time.stop(lock1) + " ms)"});
            this.refreshList[refreshID] = false;
            e01 = this.listeners;
            synchronized(this.listeners) {
               var8 = this.listeners.iterator();

               while(var8.hasNext()) {
                  listener1 = (VersionManagerListener)var8.next();

                  try {
                     listener1.onVersionsRefreshed(this);
                  } catch (Exception var18) {
                     this.log(new Object[]{"Caught listener exception:", var18});
                  }
               }

               return true;
            }
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
            try {
               VersionManager.this.startRefresh(local);
            } catch (Exception var2) {
               VersionManager.this.log(new Object[]{"Exception occured refreshing:", var2});
            }

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
         RemoteVersionList[] var6 = this.remoteLists;
         int var5 = this.remoteLists.length;

         for(int var4 = 0; var4 < var5; ++var4) {
            RemoteVersionList remoteList = var6[var4];
            container.add(new VersionManager.AsyncRawVersionListObject(remoteList));
         }

         return container.execute();
      }
   }

   public void updateVersionList() {
      if (!this.hadRemote) {
         this.asyncRefresh();
      } else {
         Iterator var2 = this.listeners.iterator();

         while(var2.hasNext()) {
            VersionManagerListener listener = (VersionManagerListener)var2.next();
            listener.onVersionsRefreshed(this);
         }
      }

   }

   public VersionSyncInfo getVersionSyncInfo(Version version) {
      if (version == null) {
         throw new NullPointerException();
      } else {
         return this.getVersionSyncInfo(version.getID());
      }
   }

   public VersionSyncInfo getVersionSyncInfo(String name) {
      if (name == null) {
         throw new NullPointerException("Cannot get sync info of NULL!");
      } else {
         if (name.startsWith("latest-")) {
            String localVersion = name.substring(7);
            name = null;
            Iterator list = this.latestVersions.entrySet().iterator();

            while(list.hasNext()) {
               Entry remoteVersion = (Entry)list.next();
               if (remoteVersion.getKey().toString().equals(localVersion)) {
                  name = ((Version)remoteVersion.getValue()).getID();
                  break;
               }
            }

            if (name == null) {
               return null;
            }
         }

         Object var10 = this.localList.getVersion(name);
         if (var10 instanceof CompleteVersion && ((CompleteVersion)var10).getInheritsFrom() != null) {
            try {
               var10 = ((CompleteVersion)var10).resolve(this, false);
            } catch (Exception var9) {
               this.log(new Object[]{"Can't resolve version " + ((Version)var10).getID(), var9});
               var10 = null;
            }
         }

         Version var11 = null;
         RemoteVersionList[] var7 = this.remoteLists;
         int var6 = this.remoteLists.length;

         for(int var5 = 0; var5 < var6; ++var5) {
            RemoteVersionList var12 = var7[var5];
            Version currentVersion = var12.getVersion(name);
            if (currentVersion != null) {
               var11 = currentVersion;
               break;
            }
         }

         return var10 == null && var11 == null ? null : new VersionSyncInfo((Version)var10, var11);
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
      Object var3 = this.versionFlushLock;
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

      ArrayList plainResult = new ArrayList();
      ArrayList result = new ArrayList();
      HashMap lookup = new HashMap();
      Version remoteList;
      Iterator var7;
      if (includeLatest) {
         var7 = this.latestVersions.values().iterator();

         while(var7.hasNext()) {
            remoteList = (Version)var7.next();
            if (filter.satisfies(remoteList)) {
               LatestVersionSyncInfo syncInfo = this.getLatestVersionSyncInfo(remoteList);
               if (syncInfo != null && !result.contains(syncInfo)) {
                  result.add(syncInfo);
               }
            }
         }
      }

      var7 = this.localList.getVersions().iterator();

      while(var7.hasNext()) {
         remoteList = (Version)var7.next();
         if (filter.satisfies(remoteList)) {
            VersionSyncInfo var15 = this.getVersionSyncInfo(remoteList);
            if (var15 != null) {
               lookup.put(remoteList.getID(), var15);
               plainResult.add(var15);
            }
         }
      }

      RemoteVersionList[] var9 = this.remoteLists;
      int var16 = this.remoteLists.length;

      for(int var14 = 0; var14 < var16; ++var14) {
         RemoteVersionList var13 = var9[var14];
         Iterator var11 = var13.getVersions().iterator();

         while(var11.hasNext()) {
            Version version = (Version)var11.next();
            if (!lookup.containsKey(version.getID()) && filter.satisfies(version)) {
               VersionSyncInfo syncInfo1 = this.getVersionSyncInfo(version);
               if (syncInfo1 != null) {
                  lookup.put(version.getID(), syncInfo1);
                  plainResult.add(syncInfo1);
               }
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

   public VersionSyncInfoContainer downloadVersion(VersionSyncInfo syncInfo, boolean ely, boolean force) throws IOException {
      VersionSyncInfoContainer container = new VersionSyncInfoContainer(syncInfo);
      CompleteVersion completeVersion = syncInfo.getCompleteVersion(force);
      if (ely) {
         CompleteVersion baseDirectory = ((ElyManager)this.manager.getComponent(ElyManager.class)).elyficate(completeVersion);
         if (syncInfo.getLocal() == completeVersion) {
            syncInfo.setLocal(baseDirectory);
         } else {
            syncInfo.setRemote(baseDirectory);
         }

         completeVersion = baseDirectory;
      }

      File baseDirectory = this.localList.getBaseDirectory();
      Set required = syncInfo.getRequiredDownloadables(baseDirectory, force, ely);
      container.addAll(required);
      this.log(new Object[]{"Required for version " + syncInfo.getID() + ':', required});
      File destination = new File(baseDirectory, "versions/" + completeVersion.getID() + "/" + completeVersion.getID() + ".jar");
      if (!force && destination.isFile()) {
         return container;
      } else {
         VersionDownloadable jarDownloadable = new VersionDownloadable(completeVersion, destination, syncInfo.getRemote() != null ? syncInfo.getRemote().getSource() : null);
         this.log(new Object[]{"Jar for " + syncInfo.getID() + ':', jarDownloadable});
         container.add(jarDownloadable);
         return container;
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

      protected VersionList.RawVersionList execute() throws AsyncObjectGotErrorException {
         try {
            return this.remoteList.getRawList();
         } catch (Exception var2) {
            VersionManager.this.log(new Object[]{"Error refreshing version list:", var2});
            throw new AsyncObjectGotErrorException(this, var2);
         }
      }
   }
}
