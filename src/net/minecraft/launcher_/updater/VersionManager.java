package net.minecraft.launcher_.updater;

import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.FileUtil;
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
import net.minecraft.launcher_.events.RefreshedVersionsListener;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionManager {
   private final LocalVersionList localVersionList;
   private final RemoteVersionList remoteVersionList;
   private final List refreshedVersionsListeners = Collections.synchronizedList(new ArrayList());
   private final Object refreshLock = new Object();
   private boolean isRefreshing;

   public VersionManager(LocalVersionList localVersionList, RemoteVersionList remoteVersionList) {
      this.localVersionList = localVersionList;
      this.remoteVersionList = remoteVersionList;
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
      List listeners = new ArrayList(this.refreshedVersionsListeners);
      Iterator iterator = listeners.iterator();

      while(iterator.hasNext()) {
         RefreshedVersionsListener listener = (RefreshedVersionsListener)iterator.next();
         listener.onVersionsRefreshing(this);
      }

      this.log("Refreshing versions...");

      try {
         this.refreshVersions_(local);
      } catch (IOException var6) {
         this.log("Refreshing failed!");
         Iterator iterator = listeners.iterator();

         while(iterator.hasNext()) {
            RefreshedVersionsListener listener = (RefreshedVersionsListener)iterator.next();
            listener.onVersionsRefreshingFailed(this);
            iterator.remove();
         }

         return;
      }

      this.log("Refreshing successful!");
   }

   public void refreshVersions_(boolean local) throws IOException {
      synchronized(this.refreshLock) {
         this.isRefreshing = true;
      }

      try {
         this.localVersionList.refreshVersions();
         if (!local) {
            this.remoteVersionList.refreshVersions();
         }
      } catch (IOException var12) {
         synchronized(this.refreshLock) {
            this.isRefreshing = false;
         }

         throw var12;
      }

      Iterator iterator = this.remoteVersionList.getVersions().iterator();

      while(iterator.hasNext()) {
         Version version = (Version)iterator.next();
         String id = version.getId();
         if (this.localVersionList.getVersion(id) != null) {
            this.localVersionList.removeVersion(id);
            this.localVersionList.addVersion(this.remoteVersionList.getCompleteVersion(id));

            try {
               this.localVersionList.saveVersion(this.localVersionList.getCompleteVersion(id));
            } catch (IOException var10) {
               synchronized(this.refreshLock) {
                  this.isRefreshing = false;
               }

               throw var10;
            }
         }
      }

      synchronized(this.refreshLock) {
         this.isRefreshing = false;
      }

      final List listeners = new ArrayList(this.refreshedVersionsListeners);
      iterator = listeners.iterator();

      while(iterator.hasNext()) {
         RefreshedVersionsListener listener = (RefreshedVersionsListener)iterator.next();
         listener.onVersionsRefreshed(this);
         iterator.remove();
      }

      if (!listeners.isEmpty()) {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               Iterator var2 = listeners.iterator();

               while(var2.hasNext()) {
                  RefreshedVersionsListener listener = (RefreshedVersionsListener)var2.next();
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
                                       if (result.isEmpty()) {
                                          var11 = this.localVersionList.getVersions().iterator();

                                          while(var11.hasNext()) {
                                             version = (Version)var11.next();
                                             if (version.getType() != null && version.getUpdatedTime() != null) {
                                                syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
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

                        syncInfo = this.getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), version);
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

         syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
         lookup.put(version.getId(), syncInfo);
         result.add(syncInfo);
      }
   }

   public VersionSyncInfo getVersionSyncInfo(Version version) {
      return this.getVersionSyncInfo(version.getId());
   }

   public VersionSyncInfo getVersionSyncInfo(String name) {
      return this.getVersionSyncInfo(this.localVersionList.getVersion(name), this.remoteVersionList.getVersion(name));
   }

   public VersionSyncInfo getVersionSyncInfo(Version localVersion, Version remoteVersion) {
      boolean installed = localVersion != null;
      boolean upToDate = installed;
      if (installed && remoteVersion != null) {
         upToDate = !remoteVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
      }

      if (localVersion instanceof CompleteVersion) {
         upToDate &= this.localVersionList.hasAllFiles((CompleteVersion)localVersion, OperatingSystem.getCurrentPlatform());
      }

      return new VersionSyncInfo(localVersion, remoteVersion, installed, upToDate);
   }

   public List getInstalledVersions() {
      List result = new ArrayList();
      Iterator var3 = this.localVersionList.getVersions().iterator();

      while(var3.hasNext()) {
         Version version = (Version)var3.next();
         if (version.getType() != null && version.getUpdatedTime() != null) {
            VersionSyncInfo syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
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
            VersionSyncInfo syncInfo = this.getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
            result.add(syncInfo);
         }
      }

      return result;
   }

   public RemoteVersionList getRemoteVersionList() {
      return this.remoteVersionList;
   }

   public LocalVersionList getLocalVersionList() {
      return this.localVersionList;
   }

   public CompleteVersion getLatestCompleteVersion(VersionSyncInfo syncInfo) throws IOException {
      if (syncInfo.getLatestSource() == VersionSyncInfo.VersionSource.REMOTE) {
         CompleteVersion result = null;
         IOException exception = null;

         try {
            result = this.remoteVersionList.getCompleteVersion(syncInfo.getLatestVersion());
         } catch (IOException var7) {
            exception = var7;

            try {
               result = this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
            } catch (IOException var6) {
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

   public DownloadableContainer downloadVersion(VersionSyncInfo syncInfo, DownloadableContainer job) throws IOException {
      CompleteVersion version = this.getLatestCompleteVersion(syncInfo);
      File baseDirectory = this.localVersionList.getBaseDirectory();
      job.addAll((Collection)version.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), baseDirectory, false));
      String jarFile = "versions/" + version.getId() + "/" + version.getId() + ".jar";
      job.addAll(new Downloadable[]{new Downloadable("https://s3.amazonaws.com/Minecraft.Download/" + jarFile, new File(baseDirectory, jarFile), false)});
      return job;
   }

   public DownloadableContainer downloadResources(DownloadableContainer job, boolean force) throws IOException {
      File baseDirectory = this.localVersionList.getBaseDirectory();
      job.addAll((Collection)this.getResourceFiles(baseDirectory, force));
      return job;
   }

   public boolean checkResources() {
      return this.checkResources(this.localVersionList.getBaseDirectory());
   }

   public boolean checkResources(File baseDirectory) {
      File file = new File(baseDirectory, "resources.list");
      if (!file.exists()) {
         return false;
      } else {
         String content;
         try {
            content = FileUtil.readFile(file);
         } catch (IOException var10) {
            var10.printStackTrace();
            return false;
         }

         String[] list = content.split("\n");
         String[] var8 = list;
         int var7 = list.length;

         for(int var6 = 0; var6 < var7; ++var6) {
            String cur = var8[var6];
            File curfile = new File(baseDirectory, "assets/" + cur);
            if (!curfile.exists()) {
               return false;
            }
         }

         return true;
      }
   }

   private Set getResourceFiles(File baseDirectory, boolean force) {
      HashSet result = new HashSet();

      try {
         StringBuilder filelist = new StringBuilder();
         boolean firstfile = true;
         URL resourceUrl = new URL("https://s3.amazonaws.com/Minecraft.Resources/");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.parse(resourceUrl.openStream());
         NodeList nodeLst = doc.getElementsByTagName("Contents");
         long start = System.nanoTime();

         for(int i = 0; i < nodeLst.getLength(); ++i) {
            Node node = nodeLst.item(i);
            if (node.getNodeType() == 1) {
               Element element = (Element)node;
               String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
               long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());
               if (size > 0L) {
                  File file = new File(baseDirectory, "assets/" + key);
                  if (!file.exists() || force) {
                     result.add(new Downloadable("https://s3.amazonaws.com/Minecraft.Resources/" + key, file, false));
                  }

                  if (firstfile) {
                     firstfile = false;
                  } else {
                     filelist.append("\n");
                  }

                  filelist.append(key);
               }
            }
         }

         FileUtil.saveFile(new File(baseDirectory, "resources.list"), filelist.toString());
         long end = System.nanoTime();
         long delta = end - start;
         U.log("Delta time to compare resources: " + delta / 1000000L + " ms ");
      } catch (Exception var20) {
         U.log("Couldn't download resources", (Throwable)var20);
      }

      return result;
   }

   public void addRefreshedVersionsListener(RefreshedVersionsListener listener) {
      this.refreshedVersionsListeners.add(listener);
   }

   public void removeRefreshedVersionsListener(RefreshedVersionsListener listener) {
      this.refreshedVersionsListeners.remove(listener);
   }

   private void log(Object w) {
      U.log("[VersionManager] " + w);
   }
}
