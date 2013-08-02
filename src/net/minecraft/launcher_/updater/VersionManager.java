package net.minecraft.launcher_.updater;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.U;

public class VersionManager
{
  private final LocalVersionList localVersionList;
  private final RemoteVersionList remoteVersionList;
  private final List<RefreshedVersionsListener> refreshedVersionsListeners = Collections.synchronizedList(new ArrayList<RefreshedVersionsListener>());
  private final Object refreshLock = new Object();
  private boolean isRefreshing;

  public VersionManager(LocalVersionList localVersionList, RemoteVersionList remoteVersionList)
  {
    this.localVersionList = localVersionList;
    this.remoteVersionList = remoteVersionList;
  }
  
  public void asyncRefresh(){
		AsyncThread.run(new Runnable(){
			public void run(){
				refreshVersions();
			}
		});
  }
  
  public void refreshVersions(){ refreshVersions(false); }
  
  public void refreshVersions(boolean local){
	  final List<RefreshedVersionsListener> listeners = new ArrayList<RefreshedVersionsListener>(this.refreshedVersionsListeners);
	  for (Iterator<RefreshedVersionsListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
	      RefreshedVersionsListener listener = iterator.next();

	       listener.onVersionsRefreshing(this);
	  }
	  
	  log("Refreshing versions...");
	  try{ refreshVersions_(local); }catch(IOException e){
		  log("Refreshing failed!");
		    for (Iterator<RefreshedVersionsListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
		      RefreshedVersionsListener listener = iterator.next();

		       listener.onVersionsRefreshingFailed(this);
		       iterator.remove();
		    }
		    return;
	  }
	  log("Refreshing successful!");
  }

  public void refreshVersions_(boolean local) throws IOException {
    synchronized (this.refreshLock) {
      this.isRefreshing = true;
    }
    try
    {
      this.localVersionList.refreshVersions();
      if(!local) this.remoteVersionList.refreshVersions();
    } catch (IOException ex) {
      synchronized (this.refreshLock) {
        this.isRefreshing = false;
      }
      throw ex;
    }

      for (Version version : this.remoteVersionList.getVersions()) {
        String id = version.getId();
        if (this.localVersionList.getVersion(id) != null) {
          this.localVersionList.removeVersion(id);
          this.localVersionList.addVersion(this.remoteVersionList.getCompleteVersion(id));
          try
          {
            ((LocalVersionList)this.localVersionList).saveVersion(this.localVersionList.getCompleteVersion(id));
          } catch (IOException ex) {
            synchronized (this.refreshLock) {
              this.isRefreshing = false;
            }
            throw ex;
          }
        }
      }

    synchronized (this.refreshLock) {
      this.isRefreshing = false;
    }

    final List<RefreshedVersionsListener> listeners = new ArrayList<RefreshedVersionsListener>(this.refreshedVersionsListeners);
    for (Iterator<RefreshedVersionsListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
      RefreshedVersionsListener listener = iterator.next();

       listener.onVersionsRefreshed(this);
       iterator.remove();
    }

    if (!listeners.isEmpty())
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run() {
          for (RefreshedVersionsListener listener : listeners)
            listener.onVersionsRefreshed(VersionManager.this);
        }
      });
  }

  public List<VersionSyncInfo> getVersions()
  {
    return getVersions(null);
  }

  public List<VersionSyncInfo> getVersions(VersionFilter filter) {
    synchronized (this.refreshLock) {
      if (this.isRefreshing) return new ArrayList<VersionSyncInfo>();
    }

    List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();
    Map<String, VersionSyncInfo> lookup = new HashMap<String, VersionSyncInfo>();
    Map<ReleaseType, Integer> counts = new EnumMap<ReleaseType, Integer>(ReleaseType.class);

    for (ReleaseType type : ReleaseType.values()) {
      counts.put(type, Integer.valueOf(0));
    }

    for (Version version : this.localVersionList.getVersions()) {
      if ((version.getType() != null) && (version.getUpdatedTime() != null) && (
        (filter == null) || ((filter.getTypes().contains(version.getType())))))
      {
        VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
        lookup.put(version.getId(), syncInfo);
        result.add(syncInfo);
      }
    }
    for (Version version : this.remoteVersionList.getVersions()) {
      if ((version.getType() != null) && (version.getUpdatedTime() != null) && 
        (!lookup.containsKey(version.getId())) && (
        (filter == null) || ((filter.getTypes().contains(version.getType())))))
      {
        VersionSyncInfo syncInfo = getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), version);
        lookup.put(version.getId(), syncInfo);
        result.add(syncInfo);

        if (filter != null) counts.put(version.getType(), Integer.valueOf(((Integer)counts.get(version.getType())).intValue() + 1));
      }
    }
    if (result.isEmpty()) {
      for (Version version : this.localVersionList.getVersions()) {
        if ((version.getType() != null) && (version.getUpdatedTime() != null)) {
          VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
          lookup.put(version.getId(), syncInfo);
          result.add(syncInfo);
        }
      }
    }

    Collections.sort(result, new Comparator<VersionSyncInfo>()
    {
      public int compare(VersionSyncInfo a, VersionSyncInfo b) {
        Version aVer = a.getLatestVersion();
        Version bVer = b.getLatestVersion();

        if ((aVer.getReleaseTime() != null) && (bVer.getReleaseTime() != null)) {
          return bVer.getReleaseTime().compareTo(aVer.getReleaseTime());
        }
        return bVer.getUpdatedTime().compareTo(aVer.getUpdatedTime());
      }
    });
    return result;
  }

  public VersionSyncInfo getVersionSyncInfo(Version version) {
    return getVersionSyncInfo(version.getId());
  }

  public VersionSyncInfo getVersionSyncInfo(String name) {
    return getVersionSyncInfo(this.localVersionList.getVersion(name), this.remoteVersionList.getVersion(name));
  }

  public VersionSyncInfo getVersionSyncInfo(Version localVersion, Version remoteVersion) {
    boolean installed = localVersion != null;
    boolean upToDate = installed;

    if ((installed) && (remoteVersion != null)) {
      upToDate = !remoteVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
    }
    if ((localVersion instanceof CompleteVersion)) {
      upToDate &= this.localVersionList.hasAllFiles((CompleteVersion)localVersion, OperatingSystem.getCurrentPlatform());
    }

    return new VersionSyncInfo(localVersion, remoteVersion, installed, upToDate);
  }

  public List<VersionSyncInfo> getInstalledVersions() {
    List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();

    for (Version version : this.localVersionList.getVersions()) {
      if ((version.getType() != null) && (version.getUpdatedTime() != null))
      {
        VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
        result.add(syncInfo);
      }
    }
    return result;
  }
  
  public List<VersionSyncInfo> getInstalledVersions(VersionFilter versionFilter) {
	  List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();

	  for (Version version : this.localVersionList.getVersions()) {
		  if ((version.getType() != null) && (version.getUpdatedTime() != null) && versionFilter.satisfies(version))
		  {
			  VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()));
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
      try
      {
        result = this.remoteVersionList.getCompleteVersion(syncInfo.getLatestVersion());
      } catch (IOException e) {
        exception = e;
        try {
          result = this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
        } catch (IOException localIOException1) {
        }
      }
      if (result != null) {
        return result;
      }
      throw exception;
    }

    return this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
  }

  public DownloadableContainer downloadVersion(VersionSyncInfo syncInfo, DownloadableContainer job) throws IOException
  {
    CompleteVersion version = getLatestCompleteVersion(syncInfo);
    File baseDirectory = this.localVersionList.getBaseDirectory();

    job.addAll(version.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), baseDirectory, false));

    String jarFile = "versions/" + version.getId() + "/" + version.getId() + ".jar";
    job.addAll(new Downloadable[] { new Downloadable("https://s3.amazonaws.com/Minecraft.Download/" + jarFile, new File(baseDirectory, jarFile), false) });

    return job;
  }

  public DownloadableContainer downloadResources(DownloadableContainer job, boolean force) throws IOException {
    File baseDirectory = this.localVersionList.getBaseDirectory();

    job.addAll(getResourceFiles(baseDirectory, force));

    return job;
  }
  
  public boolean checkResources(){
	  return checkResources(localVersionList.getBaseDirectory());
  }
  
  public boolean checkResources(File baseDirectory){
	  File file = new File(baseDirectory, "resources.list");
	  if(!file.exists()) return false;
	  
	  String content;
	  try{ content = FileUtil.readFile(file); }catch(IOException e){ e.printStackTrace(); return false; }
	  
	  String[] list = content.split("\n");
	  
	  for(String cur : list){
		  File curfile = new File(baseDirectory, "assets/" + cur);
		  if(curfile.exists()) continue;
		  return false;
	  }
	  return true;
  }

  private Set<Downloadable> getResourceFiles(File baseDirectory, boolean force) {
    Set<Downloadable> result = new HashSet<Downloadable>();
    try
    {
      StringBuilder filelist = new StringBuilder(); boolean firstfile = true;
      URL resourceUrl = new URL("https://s3.amazonaws.com/Minecraft.Resources/");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(resourceUrl.openStream());
      NodeList nodeLst = doc.getElementsByTagName("Contents");

      long start = System.nanoTime();
      for (int i = 0; i < nodeLst.getLength(); i++) {
        Node node = nodeLst.item(i);

        if (node.getNodeType() == 1) {
          Element element = (Element) node;
          String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
          String etag = element.getElementsByTagName("ETag") != null ? element.getElementsByTagName("ETag").item(0).getChildNodes().item(0).getNodeValue() : "-";
          long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());

          if (size > 0L) {
            File file = new File(baseDirectory, "assets/" + key);
            if (etag.length() > 1) {
              etag = Downloadable.getEtag(etag);
              if ((file.isFile()) && (file.length() == size)) {
                String localMd5 = FileUtil.getMD5Checksum(file);
                if (localMd5.equals(etag)) continue;
              }
            }
            if(!file.exists() || force) result.add(new Downloadable("https://s3.amazonaws.com/Minecraft.Resources/" + key, file, false));
            if(firstfile) firstfile = false; else filelist.append("\n");
            filelist.append(key);
          }
        }
      }
      FileUtil.saveFile(new File(baseDirectory, "resources.list"), filelist.toString());
      long end = System.nanoTime();
      long delta = end - start;
      U.log("Delta time to compare resources: " + delta / 1000000L + " ms ");
    } catch (Exception ex) {
      U.log("Couldn't download resources", ex);
    }

    return result;
  }

  public void addRefreshedVersionsListener(RefreshedVersionsListener listener) {
    this.refreshedVersionsListeners.add(listener);
  }

  public void removeRefreshedVersionsListener(RefreshedVersionsListener listener) {
    this.refreshedVersionsListeners.remove(listener);
  }
  
  private void log(Object w){ U.log("[VersionManager] " + w); }
  //private void log(Object w, Throwable e){ U.log("[VersionManager] " + w, e); }
}