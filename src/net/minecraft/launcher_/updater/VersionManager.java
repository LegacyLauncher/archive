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
import net.minecraft.launcher_.events.RefreshedListener;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;
import net.minecraft.launcher_.versions.VersionSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

public class VersionManager
{
  private final LocalVersionList localVersionList;
  private final RemoteVersionList remoteVersionList;
  private final ExtraVersionList extraVersionList;
  
  private final List<RefreshedListener> refreshedListeners = Collections.synchronizedList(new ArrayList<RefreshedListener>());
  private final Object refreshLock = new Object();
  private boolean isRefreshing, resourcesCalled;
  
  public VersionManager(){
	  this(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), new RemoteVersionList(), new ExtraVersionList());
  }

  public VersionManager(LocalVersionList localVersionList, RemoteVersionList remoteVersionList, ExtraVersionList extraVersionList)
  {
    this.localVersionList = localVersionList;
    this.remoteVersionList = remoteVersionList;
    this.extraVersionList = extraVersionList;
  }
  
  public void asyncRefresh(){
		AsyncThread.execute(new Runnable(){
			public void run(){
				refreshVersions();
			}
		});
  }
  
  public void refreshVersions(){ refreshVersions(false); }
  
  public void refreshVersions(boolean local){
	  final List<RefreshedListener> listeners = new ArrayList<RefreshedListener>(this.refreshedListeners);
	  for (Iterator<RefreshedListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
	      RefreshedListener listener = iterator.next();

	       listener.onVersionsRefreshing(this);
	  }
	  
	  long start = System.nanoTime();
	  log("Refreshing versions...");
	  try{ refreshVersions_(local); }catch(IOException e){
		  log("Cannot refresh versions!");
		    for (Iterator<RefreshedListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
		      RefreshedListener listener = iterator.next();

		       listener.onVersionsRefreshingFailed(this);
		       iterator.remove();
		    }
		    return;
	  }
	  long end = System.nanoTime(), diff = end - start;
	  log("Versions have been refreshed ("+(diff / 1000000L)+" ms)");
  }

  public void refreshVersions_(boolean local) throws IOException {
    synchronized (this.refreshLock) {
      this.isRefreshing = true;
    }
    try
    {
      this.localVersionList.refreshVersions();
      if(!local){
    	  this.remoteVersionList.refreshVersions();
    	  try{ this.extraVersionList.refreshVersions(); }catch(IOException e){
    		  log("Cannot refresh extra versions!", e);
    	  }
      }
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
      
      for (Version version : this.extraVersionList.getVersions()) {
          String id = version.getId();
          if (this.localVersionList.getVersion(id) != null) {
            this.localVersionList.removeVersion(id);
            this.localVersionList.addVersion(this.extraVersionList.getCompleteVersion(id));
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

    final List<RefreshedListener> listeners = new ArrayList<RefreshedListener>(this.refreshedListeners);
    for (Iterator<RefreshedListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
      RefreshedListener listener = iterator.next();

       listener.onVersionsRefreshed(this);
       iterator.remove();
    }

    if (!listeners.isEmpty())
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run() {
          for (RefreshedListener listener : listeners)
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
        VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()) );
        lookup.put(version.getId(), syncInfo);
        result.add(syncInfo);
      }
    }
    for (Version version : this.remoteVersionList.getVersions()) {
      if ((version.getType() != null) && (version.getUpdatedTime() != null) && 
        (!lookup.containsKey(version.getId())) && (
        (filter == null) || ((filter.getTypes().contains(version.getType())))))
      {
        VersionSyncInfo syncInfo = getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), version, this.extraVersionList.getVersion(version.getId()));
        lookup.put(version.getId(), syncInfo);
        result.add(syncInfo);

        if (filter != null) counts.put(version.getType(), Integer.valueOf(((Integer)counts.get(version.getType())).intValue() + 1));
      }
    }
    for (Version version : this.extraVersionList.getVersions()) {
        if ((version.getType() != null) && (version.getUpdatedTime() != null) && 
          (!lookup.containsKey(version.getId())) && (
          (filter == null) || ((filter.getTypes().contains(version.getType())))))
        {
          VersionSyncInfo syncInfo = getVersionSyncInfo(this.localVersionList.getVersion(version.getId()), this.remoteVersionList.getVersion(version.getId()), version);
          lookup.put(version.getId(), syncInfo);
          result.add(syncInfo);

          if (filter != null) counts.put(version.getType(), Integer.valueOf(((Integer)counts.get(version.getType())).intValue() + 1));
        }
      }
    if (result.isEmpty()) {
      for (Version version : this.localVersionList.getVersions()) {
        if ((version.getType() != null) && (version.getUpdatedTime() != null)) {
          VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
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
    return getVersionSyncInfo(this.localVersionList.getVersion(name), this.remoteVersionList.getVersion(name), this.extraVersionList.getVersion(name));
  }

  public VersionSyncInfo getVersionSyncInfo(Version localVersion, Version remoteVersion, Version extraVersion) {
    boolean installed = localVersion != null;
    boolean upToDate = installed;
    VersionSource remote = null, source = null;
    
    if(extraVersion != null) remote = VersionSource.EXTRA;
    else remote = VersionSource.REMOTE;
    
    if(installed) source = VersionSource.LOCAL;
    else source = remote;

    if(installed)
    	if(remoteVersion != null)
      		upToDate = !remoteVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
    	else if(extraVersion != null)
    		upToDate = !extraVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
    
    if ((localVersion instanceof CompleteVersion)) {
      upToDate &= this.localVersionList.hasAllFiles((CompleteVersion)localVersion, OperatingSystem.getCurrentPlatform());
    }

    return new VersionSyncInfo(localVersion, remoteVersion, extraVersion, installed, upToDate, remote, source);
  }

  public List<VersionSyncInfo> getInstalledVersions() {
    List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();

    for (Version version : this.localVersionList.getVersions()) {
      if ((version.getType() != null) && (version.getUpdatedTime() != null))
      {
        VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
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
			  VersionSyncInfo syncInfo = getVersionSyncInfo(version, this.remoteVersionList.getVersion(version.getId()), this.extraVersionList.getVersion(version.getId()));
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
      try
      {
        result = this.remoteVersionList.getCompleteVersion(complete);
      } catch (IOException e) {
        exception = e;
        try{
        	result = this.extraVersionList.getCompleteVersion(complete);
        } catch(IOException e0) {
        	exception = e0;
        	try {
        		result = this.localVersionList.getCompleteVersion(complete);
        	} catch (IOException e1) {
        	}
        }
      }
      if (result != null) {
        return result;
      }
      throw exception;
    }

    return this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
  }

  public void downloadVersion(VersionSyncInfo syncInfo, DownloadableContainer job) throws IOException
  {
    CompleteVersion version = getLatestCompleteVersion(syncInfo);
    File baseDirectory = this.localVersionList.getBaseDirectory();

    job.addAll(version.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), baseDirectory, false));
    
    if(!syncInfo.isOnRemote()) return;
    
    String jarFile = "versions/" + version.getId() + "/" + version.getId() + ".jar";
    job.add(new Downloadable(syncInfo.getRemoteSource().getDownloadPath() + jarFile, new File(baseDirectory, jarFile), false));
  }
  
  public DownloadableContainer downloadResources(boolean force) throws IOException {
	  DownloadableContainer job = new DownloadableContainer();
	  return this.downloadResources(job, force);
  }

  public DownloadableContainer downloadResources(DownloadableContainer job, boolean force) throws IOException {
    File baseDirectory = this.localVersionList.getBaseDirectory();

    job.addAll(getResourceFiles(baseDirectory, force));

    return job;
  }
  
  public void refreshResources(){
	  log("Refreshing resources...");
	  for(RefreshedListener l : this.refreshedListeners)
		  l.onResourcesRefreshing(this);
	  
	  long start = System.nanoTime();
	  this.getResourceFilesList(localVersionList.getBaseDirectory());
	  long end = System.nanoTime(), diff = end - start;
	  
	  for(RefreshedListener l : this.refreshedListeners)
		  l.onResourcesRefreshed(this);
	  log("Resources have been refreshed ("+(diff / 1000000L)+" ms)");
  }
  
  public void asyncRefreshResources(){
	  AsyncThread.execute(new Runnable(){
		  public void run(){
			  refreshResources();
		  }
	  });
  }
  
  public boolean checkResources(boolean local){
	  return checkResources(localVersionList.getBaseDirectory(), local);
  }
  
  public boolean checkResources(File baseDirectory, boolean local){	  
	  List<String> list = (local)? this.getLocalResourceFilesList(baseDirectory) : this.getResourceFilesList(baseDirectory);
	  
	  for(String cur : list){
		  File curfile = new File(baseDirectory, "assets/" + cur);
		  if(curfile.exists()) continue;
		  
		  return false;
	  }
	  return true;
  }
  
  private List<String> getResourceFilesList(File baseDirectory){
	  List<String> remote = null;
	  
	  if(!this.resourcesCalled)
		  try{ remote = getRemoteResourceFilesList(); resourcesCalled = true; }
	  	  catch(Exception e){ U.log("Cannot get remote resource files list. Trying to use local list.", e); }
	  
	  if(remote == null) return getLocalResourceFilesList(baseDirectory);
	  
	  try{ saveLocalResourceFilesList(baseDirectory, remote); }
	  catch(Exception e){ U.log("Cannot save resource files list locally.", e); }
	  
	  return remote;
  }
  
  private void saveLocalResourceFilesList(File baseDirectory, List<String> list) throws IOException{
	  File file = new File(baseDirectory, "resources.list");
	  if(!file.exists()){ file.getParentFile().mkdirs(); file.createNewFile(); }
	  
	  StringBuilder b = new StringBuilder();
	  boolean first = true;
	  
	  for(String cur : list)
		  if(first){ b.append(cur); first = false; }
		  else b.append("\n" + cur);
	  
	  FileUtil.saveFile(file, b.toString());
  }
  
  private List<String> getLocalResourceFilesList(File baseDirectory){
	  List<String> list = new ArrayList<String>();
	  File file = new File(baseDirectory, "resources.list");
	  if(!file.exists()) return list;
	  
	  String content;
	  try{ content = FileUtil.readFile(file); }catch(IOException e){
		  U.log("Cannot get content from resources.list");
		  return list;
	  }
	  
	  String[] lines = content.split("\n");
	  
	  for(String cur : lines)
		  list.add(cur);
	  
	  return list;
  }
  
  private List<String> getRemoteResourceFilesList() throws Exception {	  
	  List<String> list = new ArrayList<String>();
      
	  URL resourceUrl = new URL("https://s3.amazonaws.com/Minecraft.Resources/");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(resourceUrl.openStream());
      NodeList nodeLst = doc.getElementsByTagName("Contents");

      for (int i = 0; i < nodeLst.getLength(); i++) {
    	  Node node = nodeLst.item(i);

    	  if (node.getNodeType() == 1) {
    		  Element element = (Element) node;
    		  String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
    		  long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());

    		  if (size > 0L)
    			  list.add(key);
    	  }
      }
      
      return list;
  }

  private Set<Downloadable> getResourceFiles(File baseDirectory, boolean force) {
    Set<Downloadable> result = new HashSet<Downloadable>();
    List<String> list = this.getResourceFilesList(baseDirectory);
    
    for(String key : list){
    	File file = new File(baseDirectory, "assets/" + key);
    	
    	if(!file.exists() || force){
    		String url = "https://s3.amazonaws.com/Minecraft.Resources/" + key;
    		try{ result.add(new Downloadable(url, file, false)); }
    		catch(Exception e){ U.log("Cannot create Downloadable from " + url); }
    	}
    }
    return result;
  }

  public void addRefreshedListener(RefreshedListener listener) {
    this.refreshedListeners.add(listener);
  }

  public void removeRefreshedListener(RefreshedListener listener) {
    this.refreshedListeners.remove(listener);
  }
  
  private void log(Object w){ U.log("[VersionManager] " + w); }
  private void log(Object w, Throwable e){ U.log("[VersionManager] " + w, e); }
}