package net.minecraft.launcher_.updater;

import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.IOException;
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
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.events.RefreshedListener;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;
import net.minecraft.launcher_.versions.VersionSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionManager {
   private final LocalVersionList localVersionList;
   private final RemoteVersionList remoteVersionList;
   private final ExtraVersionList extraVersionList;
   private final List refreshedListeners;
   private final Object refreshLock;
   private boolean isRefreshing;
   private boolean resourcesCalled;

   public VersionManager() {
      this(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), new RemoteVersionList(), new ExtraVersionList());
   }

   public VersionManager(LocalVersionList localVersionList, RemoteVersionList remoteVersionList, ExtraVersionList extraVersionList) {
      this.refreshedListeners = Collections.synchronizedList(new ArrayList());
      this.refreshLock = new Object();
      this.localVersionList = localVersionList;
      this.remoteVersionList = remoteVersionList;
      this.extraVersionList = extraVersionList;
   }

   public void asyncRefresh() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            VersionManager.this.refreshVersions();
         }
      });
   }

   public void refreshVersions() {
      this.refreshVersions(false);
   }

   public void refreshVersions(boolean local) {
      List listeners = new ArrayList(this.refreshedListeners);
      Iterator iterator = listeners.iterator();

      while(iterator.hasNext()) {
         RefreshedListener listener = (RefreshedListener)iterator.next();
         listener.onVersionsRefreshing(this);
      }

      long start = System.nanoTime();
      this.log("Refreshing versions...");

      try {
         this.refreshVersions_(local);
      } catch (IOException var9) {
         this.log("Cannot refresh versions!");
         Iterator iterator = listeners.iterator();

         while(iterator.hasNext()) {
            RefreshedListener listener = (RefreshedListener)iterator.next();
            listener.onVersionsRefreshingFailed(this);
            iterator.remove();
         }

         return;
      }

      long end = System.nanoTime();
      long diff = end - start;
      this.log("Versions have been refreshed (" + diff / 1000000L + " ms)");
   }

   public void refreshVersions_(boolean local) throws IOException {
      synchronized(this.refreshLock) {
         this.isRefreshing = true;
      }

      try {
         this.localVersionList.refreshVersions();
         if (!local) {
            this.remoteVersionList.refreshVersions();

            try {
               this.extraVersionList.refreshVersions();
            } catch (IOException var13) {
               this.log("Cannot refresh extra versions!", var13);
            }
         }
      } catch (IOException var15) {
         synchronized(this.refreshLock) {
            this.isRefreshing = false;
         }

         throw var15;
      }

      Iterator iterator = this.remoteVersionList.getVersions().iterator();

      Version version;
      String id;
      while(iterator.hasNext()) {
         version = (Version)iterator.next();
         id = version.getId();
         if (this.localVersionList.getVersion(id) != null) {
            this.localVersionList.removeVersion(id);
            this.localVersionList.addVersion(this.remoteVersionList.getCompleteVersion(id));

            try {
               this.localVersionList.saveVersion(this.localVersionList.getCompleteVersion(id));
            } catch (IOException var12) {
               synchronized(this.refreshLock) {
                  this.isRefreshing = false;
               }

               throw var12;
            }
         }
      }

      iterator = this.extraVersionList.getVersions().iterator();

      while(iterator.hasNext()) {
         version = (Version)iterator.next();
         id = version.getId();
         if (this.localVersionList.getVersion(id) != null) {
            this.localVersionList.removeVersion(id);
            this.localVersionList.addVersion(this.extraVersionList.getCompleteVersion(id));

            try {
               this.localVersionList.saveVersion(this.localVersionList.getCompleteVersion(id));
            } catch (IOException var11) {
               synchronized(this.refreshLock) {
                  this.isRefreshing = false;
               }

               throw var11;
            }
         }
      }

      synchronized(this.refreshLock) {
         this.isRefreshing = false;
      }

      final List listeners = new ArrayList(this.refreshedListeners);
      iterator = listeners.iterator();

      while(iterator.hasNext()) {
         RefreshedListener listener = (RefreshedListener)iterator.next();
         listener.onVersionsRefreshed(this);
         iterator.remove();
      }

      if (!listeners.isEmpty()) {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               Iterator var2 = listeners.iterator();

               while(var2.hasNext()) {
                  RefreshedListener listener = (RefreshedListener)var2.next();
                  listener.onVersionsRefreshed(VersionManager.this);
               }

            }
         });
      }

   }

   public List getVersions() {
      return this.getVersions((VersionFilter)null);
   }

   public List getVersions(VersionFilter filter) {
      synchronized(this.refreshLock) {
         if (this.isRefreshing) {
            return new ArrayList();
         }
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

      Iterator var11 = this.localVersionList.getVersions().iterator();

      while(true) {
         Version version;
         VersionSyncInfo syncInfo;
         do {
            do {
               do {
                  if (!var11.hasNext()) {
                     var11 = this.remoteVersionList.getVersions().iterator();

                     while(true) {
                        do {
                           do {
                              do {
                                 do {
                                    if (!var11.hasNext()) {
                                       var11 = this.extraVersionList.getVersions().iterator();

                                       while(true) {
                                          do {
                                             do {
                                                do {
                                                   do {
                                                      if (!var11.hasNext()) {
                                                         if (result.isEmpty()) {
                                                            var11 = this.localVersionList.getVersions().iterator();

                                                            while(var11.hasNext()) {
                                                               version = (Version)var11.next();
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

                                                      version = (Version)var11.next();
                                                   } while(version.getType() == null);
                                                } while(version.getUpdatedTime() == null);
                                             } while(lookup.containsKey(version.getId()));
                                          } while(filter != null && !filter.getTypes().contains(version.getType()));

                                          syncInfo = this.getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), this.remoteVersionList.getVersion(version.getId()), version);
                                          lookup.put(version.getId(), syncInfo);
                                          result.add(syncInfo);
                                          if (filter != null) {
                                             counts.put(version.getType(), (Integer)counts.get(version.getType()) + 1);
                                          }
                                       }
                                    }

                                    version = (Version)var11.next();
                                 } while(version.getType() == null);
                              } while(version.getUpdatedTime() == null);
                           } while(lookup.containsKey(version.getId()));
                        } while(filter != null && !filter.getTypes().contains(version.getType()));

                        syncInfo = this.getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), version, this.extraVersionList.getVersion(version.getId()));
                        lookup.put(version.getId(), syncInfo);
                        result.add(syncInfo);
                        if (filter != null) {
                           counts.put(version.getType(), (Integer)counts.get(version.getType()) + 1);
                        }
                     }
                  }

                  version = (Version)var11.next();
               } while(version.getType() == null);
            } while(version.getUpdatedTime() == null);
         } while(filter != null && !filter.getTypes().contains(version.getType()));

         syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
         lookup.put(version.getId(), syncInfo);
         result.add(syncInfo);
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
         CompleteVersion result = null;
         IOException exception = null;
         Version complete = syncInfo.getLatestVersion();

         try {
            result = this.remoteVersionList.getCompleteVersion(complete);
         } catch (IOException var10) {
            exception = var10;

            try {
               result = this.extraVersionList.getCompleteVersion(complete);
            } catch (IOException var9) {
               exception = var9;

               try {
                  result = this.localVersionList.getCompleteVersion(complete);
               } catch (IOException var8) {
               }
            }
         }

         if (result != null) {
            return result;
         } else {
            throw exception;
         }
      } else {
         return this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
      }
   }

   public void downloadVersion(VersionSyncInfo syncInfo, DownloadableContainer job) throws IOException {
      CompleteVersion version = this.getLatestCompleteVersion(syncInfo);
      File baseDirectory = this.localVersionList.getBaseDirectory();
      job.addAll((Collection)version.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), baseDirectory, false));
      if (syncInfo.isOnRemote()) {
         String jarFile = "versions/" + version.getId() + "/" + version.getId() + ".jar";
         job.add(new Downloadable(syncInfo.getRemoteSource().getDownloadPath() + jarFile, new File(baseDirectory, jarFile), false));
      }
   }

   public DownloadableContainer downloadResources(boolean force) throws IOException {
      DownloadableContainer job = new DownloadableContainer();
      return this.downloadResources(job, force);
   }

   public DownloadableContainer downloadResources(DownloadableContainer job, boolean force) throws IOException {
      File baseDirectory = this.localVersionList.getBaseDirectory();
      job.addAll((Collection)this.getResourceFiles(baseDirectory, force));
      return job;
   }

   public void refreshResources() {
      this.log("Refreshing resources...");
      Iterator var2 = this.refreshedListeners.iterator();

      while(var2.hasNext()) {
         RefreshedListener l = (RefreshedListener)var2.next();
         l.onResourcesRefreshing(this);
      }

      long start = System.nanoTime();
      this.getResourceFilesList(this.localVersionList.getBaseDirectory());
      long end = System.nanoTime();
      long diff = end - start;
      Iterator var8 = this.refreshedListeners.iterator();

      while(var8.hasNext()) {
         RefreshedListener l = (RefreshedListener)var8.next();
         l.onResourcesRefreshed(this);
      }

      this.log("Resources have been refreshed (" + diff / 1000000L + " ms)");
   }

   public void asyncRefreshResources() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            VersionManager.this.refreshResources();
         }
      });
   }

   public boolean checkResources(boolean local) {
      return this.checkResources(this.localVersionList.getBaseDirectory(), local);
   }

   public boolean checkResources(File baseDirectory, boolean local) {
      List list = local ? this.getLocalResourceFilesList(baseDirectory) : this.getResourceFilesList(baseDirectory);
      Iterator var5 = list.iterator();

      while(var5.hasNext()) {
         String cur = (String)var5.next();
         File curfile = new File(baseDirectory, "assets/" + cur);
         if (!curfile.exists()) {
            return false;
         }
      }

      return true;
   }

   private List getResourceFilesList(File baseDirectory) {
      List remote = null;
      if (!this.resourcesCalled) {
         try {
            remote = this.getRemoteResourceFilesList();
            this.resourcesCalled = true;
         } catch (Exception var5) {
            U.log("Cannot get remote resource files list. Trying to use local list.", var5);
         }
      }

      if (remote == null) {
         return this.getLocalResourceFilesList(baseDirectory);
      } else {
         try {
            this.saveLocalResourceFilesList(baseDirectory, remote);
         } catch (Exception var4) {
            U.log("Cannot save resource files list locally.", var4);
         }

         return remote;
      }
   }

   private void saveLocalResourceFilesList(File baseDirectory, List list) throws IOException {
      File file = new File(baseDirectory, "resources.list");
      if (!file.exists()) {
         file.getParentFile().mkdirs();
         file.createNewFile();
      }

      StringBuilder b = new StringBuilder();
      boolean first = true;
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         String cur = (String)var7.next();
         if (first) {
            b.append(cur);
            first = false;
         } else {
            b.append("\n" + cur);
         }
      }

      FileUtil.saveFile(file, b.toString());
   }

   private List getLocalResourceFilesList(File baseDirectory) {
      List list = new ArrayList();
      File file = new File(baseDirectory, "resources.list");
      if (!file.exists()) {
         return list;
      } else {
         String content;
         try {
            content = FileUtil.readFile(file);
         } catch (IOException var10) {
            U.log("Cannot get content from resources.list");
            return list;
         }

         String[] lines = content.split("\n");
         String[] var9 = lines;
         int var8 = lines.length;

         for(int var7 = 0; var7 < var8; ++var7) {
            String cur = var9[var7];
            list.add(cur);
         }

         return list;
      }
   }

   private List getRemoteResourceFilesList() throws Exception {
      List list = new ArrayList();
      URL resourceUrl = new URL("https://s3.amazonaws.com/Minecraft.Resources/");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(resourceUrl.openStream());
      NodeList nodeLst = doc.getElementsByTagName("Contents");

      for(int i = 0; i < nodeLst.getLength(); ++i) {
         Node node = nodeLst.item(i);
         if (node.getNodeType() == 1) {
            Element element = (Element)node;
            String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
            long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());
            if (size > 0L) {
               list.add(key);
            }
         }
      }

      return list;
   }

   private Set getResourceFiles(File baseDirectory, boolean force) {
      Set result = new HashSet();
      List list = this.getResourceFilesList(baseDirectory);
      Iterator var6 = list.iterator();

      while(true) {
         String key;
         File file;
         do {
            if (!var6.hasNext()) {
               return result;
            }

            key = (String)var6.next();
            file = new File(baseDirectory, "assets/" + key);
         } while(file.exists() && !force);

         String url = "https://s3.amazonaws.com/Minecraft.Resources/" + key;

         try {
            result.add(new Downloadable(url, file, false));
         } catch (Exception var10) {
            U.log("Cannot create Downloadable from " + url);
         }
      }
   }

   public void addRefreshedListener(RefreshedListener listener) {
      this.refreshedListeners.add(listener);
   }

   public void removeRefreshedListener(RefreshedListener listener) {
      this.refreshedListeners.remove(listener);
   }

   private void log(Object w) {
      U.log("[VersionManager] " + w);
   }

   private void log(Object w, Throwable e) {
      U.log("[VersionManager] " + w, e);
   }
}
