package net.minecraft.launcher.updater;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncObject;
import com.turikhay.util.async.AsyncObjectContainer;
import com.turikhay.util.async.AsyncThread;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.VersionSource;
import org.apache.commons.io.IOUtils;

public class VersionManager {
   private static final String ASSETS_REPO = "http://resources.download.minecraft.net/";
   private final Gson gson;
   private LocalVersionList localVersionList;
   private RemoteVersionList remoteVersionList;
   private ExtraVersionList extraVersionList;
   private final List refreshedListeners;
   private final List versionRefreshes;
   private final List resourceRefreshes;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher$versions$VersionSource;

   public VersionManager() throws IOException {
      this(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), new RemoteVersionList(), new ExtraVersionList());
   }

   public VersionManager(LocalVersionList localVersionList, RemoteVersionList remoteVersionList, ExtraVersionList extraVersionList) {
      this.gson = new Gson();
      this.refreshedListeners = Collections.synchronizedList(new ArrayList());
      this.versionRefreshes = Collections.synchronizedList(new ArrayList());
      this.resourceRefreshes = Collections.synchronizedList(new ArrayList());
      this.localVersionList = localVersionList;
      this.remoteVersionList = remoteVersionList;
      this.extraVersionList = extraVersionList;
   }

   public void recreate(LocalVersionList localVersionList, RemoteVersionList remoteVersionList, ExtraVersionList extraVersionList) {
      this.localVersionList = localVersionList;
      this.remoteVersionList = remoteVersionList;
      this.extraVersionList = extraVersionList;
      List listeners = new ArrayList(this.refreshedListeners);
      Iterator iterator = listeners.iterator();

      while(iterator.hasNext()) {
         RefreshedListener listener = (RefreshedListener)iterator.next();
         listener.onVersionManagerUpdated(this);
      }

   }

   public void recreate() throws IOException {
      this.recreate(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), this.remoteVersionList, this.extraVersionList);
   }

   public void refreshVersions(boolean local) {
      long start = System.nanoTime();
      log("Refreshing versions...");
      Iterator var9 = this.refreshedListeners.iterator();

      RefreshedListener listener;
      while(var9.hasNext()) {
         listener = (RefreshedListener)var9.next();
         listener.onVersionsRefreshing(this);
      }

      long end;
      long diff;
      try {
         this.silentlyRefreshVersions(local);
      } catch (VersionManager.RefreshedException var11) {
         end = System.nanoTime();
         diff = end - start;
         log("Versions refresh has been cancelled (" + diff / 1000000L + " ms)");
         return;
      } catch (Throwable var12) {
         Iterator var10 = this.refreshedListeners.iterator();

         while(var10.hasNext()) {
            RefreshedListener listener = (RefreshedListener)var10.next();
            listener.onVersionsRefreshed(this);
         }

         log("Cannot refresh versions!", var12);
         return;
      }

      var9 = this.refreshedListeners.iterator();

      while(var9.hasNext()) {
         listener = (RefreshedListener)var9.next();
         listener.onVersionsRefreshed(this);
      }

      end = System.nanoTime();
      diff = end - start;
      log("Versions have been refreshed (" + diff / 1000000L + " ms)");
   }

   public void refreshVersions() {
      this.refreshVersions(false);
   }

   public void asyncRefresh(final Runnable onComplete, final boolean local) {
      AsyncThread.execute(new Runnable() {
         public void run() {
            VersionManager.this.refreshVersions(local);
            if (onComplete != null) {
               onComplete.run();
            }

         }
      });
   }

   public void asyncRefresh(Runnable onComplete) {
      this.asyncRefresh(onComplete, false);
   }

   public void asyncRefresh(boolean local) {
      this.asyncRefresh((Runnable)null, local);
   }

   public void asyncRefresh() {
      this.asyncRefresh((Runnable)null, false);
   }

   private void silentlyRefreshVersions(boolean local) throws Throwable {
      Short rand = U.shortRandom();
      synchronized(this.versionRefreshes) {
         if (!this.versionRefreshes.isEmpty()) {
            this.versionRefreshes.clear();
         }

         this.versionRefreshes.add(U.shortRandom());
      }

      Throwable e = null;

      try {
         this.desyncRefreshVersions(local, rand);
      } catch (Throwable var5) {
         e = var5;
      }

      this.versionRefreshes.remove(rand);
      if (e != null) {
         throw e;
      }
   }

   /** @deprecated */
   @Deprecated
   private void desyncRefreshVersions(boolean local, Short rand) throws IOException, VersionManager.RefreshedException {
      this.localVersionList.refreshVersions();
      if (!local) {
         AsyncObjectContainer asyncContainer = new AsyncObjectContainer();
         AsyncObject remoteRawAsync = new AsyncObject() {
            protected VersionList.RawVersionList execute() {
               try {
                  return VersionManager.this.remoteVersionList.getRawList();
               } catch (IOException var2) {
                  throw new RuntimeException(var2);
               }
            }
         };
         AsyncObject extraRawAsync = new AsyncObject() {
            protected VersionList.RawVersionList execute() {
               try {
                  return VersionManager.this.extraVersionList.getRawList();
               } catch (IOException var2) {
                  throw new RuntimeException(var2);
               }
            }
         };
         asyncContainer.add(remoteRawAsync);
         asyncContainer.add(extraRawAsync);
         Map results = asyncContainer.execute();
         VersionList.RawVersionList remoteRaw = (VersionList.RawVersionList)results.get(remoteRawAsync);
         VersionList.RawVersionList extraRaw = (VersionList.RawVersionList)results.get(extraRawAsync);
         if (!this.versionRefreshes.contains(rand)) {
            throw new VersionManager.RefreshedException((VersionManager.RefreshedException)null);
         } else {
            synchronized(this.versionRefreshes) {
               if (remoteRaw != null) {
                  this.remoteVersionList.refreshVersions(remoteRaw);
               }

               if (extraRaw != null) {
                  this.extraVersionList.refreshVersions(extraRaw);
               }

            }
         }
      }
   }

   public void cancelVersionRefresh() {
      log("Cancelling version refresh...");
      this.versionRefreshes.clear();
      this.asyncRefresh(true);
   }

   public void updateVersionList() {
      synchronized(this.versionRefreshes) {
         Iterator var3 = this.refreshedListeners.iterator();

         while(var3.hasNext()) {
            RefreshedListener listener = (RefreshedListener)var3.next();
            listener.onVersionsRefreshed(this);
         }

      }
   }

   public List getVersions() {
      return this.getVersions(TLauncher.getInstance() == null ? null : TLauncher.getInstance().getSettings().getVersionFilter());
   }

   public List getVersions(VersionFilter filter) {
      if (!this.versionRefreshes.isEmpty()) {
         return null;
      } else {
         if (filter == null) {
            filter = new VersionFilter();
         }

         List result = new ArrayList();
         Map lookup = new HashMap();
         Map counts = new EnumMap(ReleaseType.class);
         ReleaseType[] var8;
         int var7 = (var8 = ReleaseType.values()).length;

         for(int var6 = 0; var6 < var7; ++var6) {
            ReleaseType type = var8[var6];
            counts.put(type, 0);
         }

         Iterator var10 = this.localVersionList.getVersions().iterator();

         Version version;
         VersionSyncInfo syncInfo;
         while(var10.hasNext()) {
            version = (Version)var10.next();
            if (filter.satisfies(version)) {
               syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
               lookup.put(version.getId(), syncInfo);
               result.add(syncInfo);
            }
         }

         var10 = this.remoteVersionList.getVersions().iterator();

         while(var10.hasNext()) {
            version = (Version)var10.next();
            if (!lookup.containsKey(version.getId()) && filter.satisfies(version)) {
               syncInfo = this.getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), version, this.extraVersionList.getVersion(version.getId()));
               lookup.put(version.getId(), syncInfo);
               result.add(syncInfo);
               counts.put(version.getType(), (Integer)counts.get(version.getType()) + 1);
            }
         }

         var10 = this.extraVersionList.getVersions().iterator();

         while(var10.hasNext()) {
            version = (Version)var10.next();
            if (!lookup.containsKey(version.getId()) && filter.satisfies(version)) {
               syncInfo = this.getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), this.remoteVersionList.getVersion(version.getId()), version);
               lookup.put(version.getId(), syncInfo);
               result.add(syncInfo);
               counts.put(version.getType(), (Integer)counts.get(version.getType()) + 1);
            }
         }

         if (result.isEmpty()) {
            var10 = this.localVersionList.getVersions().iterator();

            while(var10.hasNext()) {
               version = (Version)var10.next();
               if (version.getType() != null && version.getUpdatedTime() != null) {
                  syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
                  lookup.put(version.getId(), syncInfo);
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
   }

   public VersionSyncInfo getVersionSyncInfo(Version version) {
      return this.getVersionSyncInfo(version.getId());
   }

   public VersionSyncInfo getVersionSyncInfo(String name) {
      return this.getVersionSyncInfo(this.localVersionList.getVersion(name), this.remoteVersionList.getVersion(name), this.extraVersionList.getVersion(name));
   }

   public VersionSyncInfo getVersionSyncInfo(Version localVersion, Version remoteVersion, Version extraVersion) {
      boolean installed = localVersion != null;
      boolean upToDate = installed;
      VersionSource remote = null;
      VersionSource source = null;
      if (extraVersion != null) {
         remote = VersionSource.EXTRA;
      } else {
         remote = VersionSource.REMOTE;
      }

      if (installed) {
         source = VersionSource.LOCAL;
      } else {
         source = remote;
      }

      if (installed) {
         if (remoteVersion != null) {
            upToDate = !remoteVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
         } else if (extraVersion != null) {
            upToDate = !extraVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
         }
      }

      if (localVersion instanceof CompleteVersion) {
         upToDate &= this.localVersionList.hasAllFiles((CompleteVersion)localVersion, OperatingSystem.getCurrentPlatform());
      }

      return new VersionSyncInfo(localVersion, remoteVersion, extraVersion, installed, upToDate, remote, source);
   }

   public List getInstalledVersions() {
      List result = new ArrayList();
      Iterator var3 = this.localVersionList.getVersions().iterator();

      while(var3.hasNext()) {
         Version version = (Version)var3.next();
         if (version.getType() != null && version.getUpdatedTime() != null) {
            VersionSyncInfo syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
            result.add(syncInfo);
         }
      }

      return result;
   }

   public List getInstalledVersions(VersionFilter versionFilter) {
      List result = new ArrayList();
      Iterator var4 = this.localVersionList.getVersions().iterator();

      while(var4.hasNext()) {
         Version version = (Version)var4.next();
         if (version.getType() != null && version.getUpdatedTime() != null && versionFilter.satisfies(version)) {
            VersionSyncInfo syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
            result.add(syncInfo);
         }
      }

      return result;
   }

   public RemoteVersionList getRemoteVersionList() {
      return this.remoteVersionList;
   }

   public ExtraVersionList getExtraVersionList() {
      return this.extraVersionList;
   }

   public LocalVersionList getLocalVersionList() {
      return this.localVersionList;
   }

   public CompleteVersion getLatestCompleteVersion(VersionSyncInfo syncInfo) throws IOException {
      if (syncInfo.getLatestSource() != VersionSource.LOCAL) {
         Version complete = syncInfo.getLatestVersion();
         VersionSource source = syncInfo.getLatestSource();
         CompleteVersion result;
         switch($SWITCH_TABLE$net$minecraft$launcher$versions$VersionSource()[source.ordinal()]) {
         case 1:
            result = this.localVersionList.getCompleteVersion(complete);
            break;
         case 2:
            result = this.remoteVersionList.getCompleteVersion(complete);
            break;
         case 3:
            result = this.extraVersionList.getCompleteVersion(complete);
            break;
         default:
            throw new IllegalStateException("Unknown source:" + source);
         }

         if (result != null) {
            return result;
         }
      }

      return this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
   }

   public CompleteVersion getRemoteCompleteVersion(VersionSyncInfo syncInfo) throws IOException {
      CompleteVersion result = null;
      Version complete = syncInfo.getRemoteVersion();
      VersionSource source = syncInfo.getRemoteSource();
      if (complete != null && source != null) {
         switch($SWITCH_TABLE$net$minecraft$launcher$versions$VersionSource()[source.ordinal()]) {
         case 2:
            result = this.remoteVersionList.getCompleteVersion(complete);
            break;
         case 3:
            result = this.extraVersionList.getCompleteVersion(complete);
            break;
         default:
            throw new IllegalStateException("Unknown source:" + source);
         }
      }

      return result != null ? result : this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
   }

   public void downloadVersion(DownloadableContainer job, VersionSyncInfo syncInfo, boolean force) throws IOException {
      CompleteVersion version = force ? this.getRemoteCompleteVersion(syncInfo) : this.getLatestCompleteVersion(syncInfo);
      File baseDirectory = this.localVersionList.getBaseDirectory();
      job.addAll((Collection)version.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), syncInfo.getRemoteSource(), baseDirectory, force));
      if (syncInfo.isOnRemote()) {
         String url = version.getUrl();
         String id = version.getId();
         String o_id = version.getOriginalID();
         String jarFile = "versions/";
         String saveFile = jarFile;
         String downloadPath;
         if (url != null) {
            downloadPath = version.getUrl();
            jarFile = "";
            saveFile = saveFile + id + "/" + id + ".jar";
         } else if (o_id != null) {
            downloadPath = VersionSource.REMOTE.getSelectedRepo();
            jarFile = jarFile + o_id + "/" + o_id + ".jar";
            saveFile = saveFile + id + "/" + id + ".jar";
         } else {
            downloadPath = syncInfo.getRemoteSource().getSelectedRepo();
            jarFile = jarFile + id + "/" + id + ".jar";
            saveFile = jarFile;
         }

         Downloadable d = new Downloadable(downloadPath + jarFile, new File(baseDirectory, saveFile), force);
         d.setAdditionalDestinations(new File[]{new File(d.getDestination() + ".bak")});
         job.add(d);
      }
   }

   public DownloadableContainer downloadResources(DownloadableContainer job, CompleteVersion version, List list, boolean force) throws IOException {
      File baseDirectory = this.localVersionList.getBaseDirectory();
      job.addAll((Collection)this.getResourceFiles(version, baseDirectory, list));
      return job;
   }

   private Set getResourceFiles(CompleteVersion version, File baseDirectory, List list) {
      Set result = new HashSet();
      File objectsFolder = new File(baseDirectory, "assets/objects");
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         AssetIndex.AssetObject object = (AssetIndex.AssetObject)var7.next();
         String filename = object.getFilename();
         String url = "http://resources.download.minecraft.net/" + filename;
         File file = new File(objectsFolder, filename);

         try {
            Downloadable d = new Downloadable(url, file, false);
            d.setFast(true);
            result.add(d);
         } catch (MalformedURLException var12) {
            log("Cannot create downloadable resource from URL:", url, var12);
         }
      }

      return result;
   }

   public boolean refreshResources(CompleteVersion version, boolean local) {
      long start = System.nanoTime();
      log("Refreshing resources...");

      long end;
      long diff;
      try {
         this.getResourceFilesList(version, this.localVersionList.getBaseDirectory(), local);
      } catch (VersionManager.RefreshedException var10) {
         end = System.nanoTime();
         diff = end - start;
         log("Resource refresh has been cancelled (" + diff / 1000000L + " ms)");
         return false;
      } catch (Throwable var11) {
         log("Cannot refresh resources!", var11);
         return false;
      }

      end = System.nanoTime();
      diff = end - start;
      log("Resources have been refreshed (" + diff / 1000000L + " ms)");
      return true;
   }

   private List getResourceFilesList(CompleteVersion version, File baseDirectory, boolean local) throws VersionManager.RefreshedException {
      Short rand = U.shortRandom();
      synchronized(this.resourceRefreshes) {
         if (!this.resourceRefreshes.isEmpty()) {
            this.resourceRefreshes.clear();
         }

         this.resourceRefreshes.add(U.shortRandom());
      }

      Object var5 = null;

      try {
         return this.desyncResourceFilesList(version, baseDirectory, local, rand);
      } catch (VersionManager.RefreshedException var7) {
         this.resourceRefreshes.remove(rand);
         throw var7;
      }
   }

   /** @deprecated */
   @Deprecated
   private List desyncResourceFilesList(CompleteVersion version, File baseDirectory, boolean local, Short rand) throws VersionManager.RefreshedException {
      List list = null;
      if (!local) {
         try {
            list = this.getRemoteResourceFilesList(version, baseDirectory, true);
         } catch (Exception var8) {
            log("Cannot get remote assets list. Trying to use the local one.", var8);
         }
      }

      if (!this.resourceRefreshes.contains(rand)) {
         throw new VersionManager.RefreshedException((VersionManager.RefreshedException)null);
      } else {
         if (list == null) {
            list = this.getLocalResourceFilesList(version, baseDirectory);
         }

         if (list == null) {
            try {
               list = this.getRemoteResourceFilesList(version, baseDirectory, true);
            } catch (Exception var7) {
               log("Gave up trying to get assets list.", var7);
            }
         }

         return list;
      }
   }

   private List getLocalResourceFilesList(CompleteVersion version, File baseDirectory) {
      List result = new ArrayList();
      String indexName = version.getAssets();
      File indexesFolder = new File(baseDirectory, "assets/indexes/");
      File indexFile = new File(indexesFolder, indexName + ".json");
      log("Reading indexes from file", indexFile);

      String json;
      try {
         json = FileUtil.readFile(indexFile);
      } catch (IOException var12) {
         log("Cannot read local resource files list for index:", indexName, var12);
         return null;
      }

      AssetIndex index = null;

      try {
         index = (AssetIndex)this.gson.fromJson(json, AssetIndex.class);
      } catch (JsonSyntaxException var11) {
         log("JSON file is invalid", var11);
      }

      if (index == null) {
         log("Cannot read data from JSON file.");
         return null;
      } else {
         Iterator var10 = index.getUniqueObjects().iterator();

         while(var10.hasNext()) {
            AssetIndex.AssetObject object = (AssetIndex.AssetObject)var10.next();
            result.add(object);
         }

         return result;
      }
   }

   private List getRemoteResourceFilesList(CompleteVersion version, File baseDirectory, boolean save) throws IOException {
      List result = new ArrayList();
      String indexName = version.getAssets();
      if (indexName == null) {
         indexName = "legacy";
      }

      File assets = new File(baseDirectory, "assets");
      File indexesFolder = new File(assets, "indexes");
      File indexFile = new File(indexesFolder, indexName + ".json");
      URL indexUrl = new URL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + indexName + ".json");
      InputStream inputStream = indexUrl.openConnection().getInputStream();
      log("Reading indexes from URL", indexUrl);
      String json = IOUtils.toString(inputStream);
      if (save) {
         FileUtil.writeFile(indexFile, json);
      }

      AssetIndex index = (AssetIndex)this.gson.fromJson(json, AssetIndex.class);
      Iterator var14 = index.getUniqueObjects().iterator();

      while(var14.hasNext()) {
         AssetIndex.AssetObject object = (AssetIndex.AssetObject)var14.next();
         result.add(object);
      }

      return result;
   }

   public List checkResources(CompleteVersion version, File baseDirectory, boolean local, boolean fast) {
      log("Checking resources...");
      List r = new ArrayList();
      List list;
      if (local) {
         list = this.getLocalResourceFilesList(version, baseDirectory);
      } else {
         try {
            list = this.getResourceFilesList(version, baseDirectory, true);
         } catch (VersionManager.RefreshedException var9) {
            list = this.getLocalResourceFilesList(version, baseDirectory);
         }
      }

      if (list == null) {
         log("Cannot get assets list. Aborting.");
         return r;
      } else {
         log("Fast comparing:", fast);
         Iterator var8 = list.iterator();

         while(var8.hasNext()) {
            AssetIndex.AssetObject resource = (AssetIndex.AssetObject)var8.next();
            if (!this.checkResource(baseDirectory, resource, fast)) {
               r.add(resource);
            }
         }

         return r;
      }
   }

   public List checkResources(CompleteVersion version) {
      return this.checkResources(version, this.localVersionList.getBaseDirectory(), false, true);
   }

   private boolean checkResource(File baseDirectory, AssetIndex.AssetObject local, boolean fast) {
      String path = local.getFilename();
      File file = new File(baseDirectory, "assets/objects/" + path);
      if (file.isFile() && file.length() != 0L) {
         if (fast) {
            return true;
         } else {
            String hash = FileUtil.getChecksum(file, "SHA-1");
            return local.getHash() == hash;
         }
      } else {
         return false;
      }
   }

   public void addRefreshedListener(RefreshedListener listener) {
      if (this.versionRefreshes.isEmpty()) {
         listener.onVersionsRefreshed(this);
      } else {
         listener.onVersionsRefreshing(this);
      }

      this.refreshedListeners.add(listener);
   }

   public void removeRefreshedListener(RefreshedListener listener) {
      this.refreshedListeners.remove(listener);
   }

   private static void log(Object... w) {
      U.log("[VersionManager]", w);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher$versions$VersionSource() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher$versions$VersionSource;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[VersionSource.values().length];

         try {
            var0[VersionSource.EXTRA.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[VersionSource.LOCAL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[VersionSource.REMOTE.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$net$minecraft$launcher$versions$VersionSource = var0;
         return var0;
      }
   }

   private class RefreshedException extends Exception {
      private static final long serialVersionUID = -614365722177994706L;

      private RefreshedException() {
      }

      // $FF: synthetic method
      RefreshedException(VersionManager.RefreshedException var2) {
         this();
      }
   }
}
