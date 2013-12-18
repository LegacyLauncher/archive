package net.minecraft.launcher.updater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.AssetIndex.AssetObject;
import net.minecraft.launcher.updater.VersionList.RawVersionList;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.VersionSource;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncObject;
import com.turikhay.util.async.AsyncObjectContainer;

public class VersionManager {
	private static final String ASSETS_REPO = "http://resources.download.minecraft.net/";
	
	private final Gson gson = new Gson();
	
	private LocalVersionList localVersionList;
	private RemoteVersionList remoteVersionList;
	private ExtraVersionList extraVersionList;
  
	private final List<RefreshedListener> refreshedListeners = Collections.synchronizedList(new ArrayList<RefreshedListener>());
	private final List<Short>
		versionRefreshes = Collections.synchronizedList(new ArrayList<Short>()),
		resourceRefreshes = Collections.synchronizedList(new ArrayList<Short>());
  
  public VersionManager() throws IOException {
	  this(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), new RemoteVersionList(), new ExtraVersionList());
  }

  public VersionManager(LocalVersionList localVersionList, RemoteVersionList remoteVersionList, ExtraVersionList extraVersionList)
  {	  
	  this.localVersionList = localVersionList;
	  this.remoteVersionList = remoteVersionList;
	  this.extraVersionList = extraVersionList;
  }
  
  public void recreate(LocalVersionList localVersionList, RemoteVersionList remoteVersionList, ExtraVersionList extraVersionList){
	  this.localVersionList = localVersionList;
	  this.remoteVersionList = remoteVersionList;
	  this.extraVersionList = extraVersionList;
	  
	  final List<RefreshedListener> listeners = new ArrayList<RefreshedListener>(this.refreshedListeners);
	  for (Iterator<RefreshedListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
	      RefreshedListener listener = iterator.next();

	       listener.onVersionManagerUpdated(this);
	  }
  }
  public void recreate() throws IOException{ recreate(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), this.remoteVersionList, this.extraVersionList); }
    
  public void refreshVersions(boolean local) {
	  long start = System.nanoTime(), end, diff;
	  log("Refreshing versions...");
	  
	  for(RefreshedListener listener : this.refreshedListeners) listener.onVersionsRefreshing(this);
	  
	  try{ silentlyRefreshVersions(local); }
	  catch(RefreshedException re){
		  end = System.nanoTime();
		  diff = end - start;
		  
		  log("Versions refresh has been cancelled ("+(diff / 1000000L)+" ms)");
		  return;
	  }
	  catch(Throwable e){		  
		  for(RefreshedListener listener : this.refreshedListeners) listener.onVersionsRefreshed(this);
		  log("Cannot refresh versions!", e);
		  return;
	  }
	  
	  for(RefreshedListener listener : this.refreshedListeners) listener.onVersionsRefreshed(this);
	  
	  end = System.nanoTime();
	  diff = end - start;
	  
	  log("Versions have been refreshed ("+(diff / 1000000L)+" ms)");
  }
  public void refreshVersions(){ refreshVersions(false); }
  
  public void asyncRefresh(final boolean local) {
	  AsyncThread.execute(new Runnable(){
		  public void run(){
			  refreshVersions(local);
		  }
	  });
  }
  public void asyncRefresh(){ asyncRefresh(false); }
  
  private void silentlyRefreshVersions(boolean local) throws Throwable {
	  Short rand = Short.valueOf(U.shortRandom());
	  
	  synchronized(versionRefreshes){
		  while(versionRefreshes.contains(rand))
			  rand = Short.valueOf(U.shortRandom());
		  
		  versionRefreshes.add(rand);
	  }
	  
	  Throwable e = null;
	  try{ desyncRefreshVersions(local, rand); }
	  catch(Throwable e0){ e = e0; }

	  versionRefreshes.remove(rand);
	  if(e != null) throw e;
  }
  
  @Deprecated
  private void desyncRefreshVersions(boolean local, Short rand) throws IOException, RefreshedException{
	  this.localVersionList.refreshVersions();
	  
	  if(local) return;
	  
	  AsyncObjectContainer<RawVersionList> asyncContainer = new AsyncObjectContainer<RawVersionList>();
	  
	  AsyncObject<RawVersionList>
	  	remoteRawAsync = new AsyncObject<RawVersionList>(){
	  		protected RawVersionList execute() {
	  			try {
					return remoteVersionList.getRawList();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	  		}
	  	},
	  	extraRawAsync = new AsyncObject<RawVersionList>(){
	  		protected RawVersionList execute() {
	  			try {
					return extraVersionList.getRawList();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	  		}
	  	};
	  
	  asyncContainer.add(remoteRawAsync);
	  asyncContainer.add(extraRawAsync);
	  
	  Map<AsyncObject<RawVersionList>, RawVersionList> results = asyncContainer.execute();
	  
	  RawVersionList remoteRaw = results.get(remoteRawAsync), extraRaw = results.get(extraRawAsync);
	  
	  if(!versionRefreshes.contains(rand))
		  throw new RefreshedException();
	  
	  synchronized(versionRefreshes){		  
		  if(remoteRaw != null) this.remoteVersionList.refreshVersions(remoteRaw);
		  if(extraRaw != null) this.extraVersionList.refreshVersions(extraRaw);
	  }
  }
  
  public void cancelVersionRefresh() {
	  log("Cancelling version refresh...");
	  
	  versionRefreshes.clear();
	  this.asyncRefresh(true);
  }

  public List<VersionSyncInfo> getVersions() {
	  return getVersions(null);
  }

  public List<VersionSyncInfo> getVersions(VersionFilter filter) {
	  if(!versionRefreshes.isEmpty()) return new ArrayList<VersionSyncInfo>();

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

			  if (filter != null) counts.put(version.getType(), Integer.valueOf(counts.get(version.getType()).intValue() + 1));
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

	  Collections.sort(result, new Comparator<VersionSyncInfo>() {
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
    
    if ((localVersion instanceof CompleteVersion))
      upToDate &= this.localVersionList.hasAllFiles((CompleteVersion)localVersion, OperatingSystem.getCurrentPlatform());

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
		  CompleteVersion result;
		  
		  Version complete = syncInfo.getLatestVersion();
		  VersionSource source = syncInfo.getLatestSource();
		  
		  switch(source){
		  case EXTRA:
			  result = this.extraVersionList.getCompleteVersion(complete);
			  break;
		  case LOCAL:
			  result = this.localVersionList.getCompleteVersion(complete);
			  break;
		  case REMOTE:
			  result = this.remoteVersionList.getCompleteVersion(complete);
			  break;
		  default:
			  throw new IllegalStateException("Unknown source:" + source);
		  }
		  
		  if(result != null)
			  return result;
	  }

	  return this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
  }

  public void downloadVersion(DownloadableContainer job, VersionSyncInfo syncInfo, boolean force) throws IOException
  {
    CompleteVersion version = getLatestCompleteVersion(syncInfo);
    File baseDirectory = this.localVersionList.getBaseDirectory();

    job.addAll(version.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), syncInfo.getRemoteSource(), baseDirectory, force));
    
    if(!syncInfo.isOnRemote()) return;
    
    String url = version.getUrl(),
    	id = version.getId(), o_id = version.getOriginalID(),
    	downloadPath, jarFile = "versions/", saveFile = jarFile;
    
    if(url != null){
    	downloadPath = version.getUrl();
    	jarFile = "";
    	saveFile += id + "/" + id + ".jar";
    }else if(o_id != null){
    	// Original versions should be downloaded from the default repo
    	downloadPath = VersionSource.REMOTE.getSelectedRepo();
    	jarFile += o_id + "/" + o_id + ".jar";
    	saveFile += id + "/" + id + ".jar";
    } else {
    	downloadPath = syncInfo.getRemoteSource().getSelectedRepo();
    	jarFile += id + "/" + id + ".jar";
    	saveFile = jarFile;
    }
    
    U.log("Latest source:", syncInfo.getLatestSource());
    	
    Downloadable d = new Downloadable(downloadPath + jarFile, new File(baseDirectory, saveFile), force);
    d.setAdditionalDestinations(new File[]{ new File(d.getDestination() + ".bak") });
    
    job.add(d);
  }

  public DownloadableContainer downloadResources(DownloadableContainer job, CompleteVersion version, List<AssetObject> list, boolean force) throws IOException {
	  File baseDirectory = this.localVersionList.getBaseDirectory();

	  job.addAll(getResourceFiles(version, baseDirectory, list));

	  return job;
  }
  
  private Set<Downloadable> getResourceFiles(CompleteVersion version, File baseDirectory, List<AssetObject> list) {
	  Set<Downloadable> result = new HashSet<Downloadable>();
	    
	  File objectsFolder = new File(baseDirectory, "assets/objects");
	    
	  for(AssetObject object : list){  	
		  String filename = object.getFilename(), url = ASSETS_REPO + filename;
		  File file = new File(objectsFolder, filename);
	        
		  try{ result.add(new Downloadable(url, file, false)); }
		  catch(MalformedURLException e){ log("Cannot create downloadable resource from URL:", url, e); }
	  }
	  
	  return result;
  }
  
  public boolean refreshResources(CompleteVersion version, boolean local){
	  long start = System.nanoTime(), end, diff;
	  log("Refreshing resources...");
	  
	  for(RefreshedListener l : this.refreshedListeners) l.onResourcesRefreshing(this);
	  
	  try{ this.getResourceFilesList(version, localVersionList.getBaseDirectory(), local); }
	  catch(RefreshedException re){
		  end = System.nanoTime();
		  diff = end - start;
		  
		  log("Resource refresh has been cancelled ("+(diff / 1000000L)+" ms)");
		  return false;
	  }
	  catch(Throwable e){
		  log("Cannot refresh resources!", e);
		  return false;
	  }
	  
	  for(RefreshedListener l : this.refreshedListeners) l.onResourcesRefreshed(this);
	  
	  end = System.nanoTime();
	  diff = end - start;
	  log("Resources have been refreshed ("+(diff / 1000000L)+" ms)");
	  
	  return true;
  }
  
  private List<AssetObject> getResourceFilesList(CompleteVersion version, File baseDirectory, boolean local) throws RefreshedException {
	  Short rand = U.shortRandom();
	  synchronized(resourceRefreshes){
		  while(resourceRefreshes.contains(rand))
			  rand = Short.valueOf(U.shortRandom());
		  
		  resourceRefreshes.add(rand);
	  }
	  
	  RefreshedException e = null;
	  try{ return desyncResourceFilesList(version, baseDirectory, local, rand); }
	  catch(RefreshedException e0){ e = e0; }
	  
	  resourceRefreshes.remove(rand);
	  throw e;
  }
  
  @Deprecated
  private List<AssetObject> desyncResourceFilesList(CompleteVersion version, File baseDirectory, boolean local, Short rand) throws RefreshedException {	  
	  List<AssetObject> list = null;
	  
	  if(!local)
		  try{
			  list = getRemoteResourceFilesList(version, baseDirectory, true);
		  }catch(Exception e){ log("Cannot get remote assets list. Trying to use the local one.", e); }
	  
	  if(!resourceRefreshes.contains(rand))
		  throw new RefreshedException();
	  
	  if(list == null)
		  list = getLocalResourceFilesList(version, baseDirectory);
	  
	  if(list == null)
		  try{
			  list = getRemoteResourceFilesList(version, baseDirectory, true);
		  }catch(Exception e){ log("Gave up trying to get assets list.", e); }
	  
	  return list;
  }
  
  private List<AssetObject> getLocalResourceFilesList(CompleteVersion version, File baseDirectory){
	  List<AssetObject> result = new ArrayList<AssetObject>();
	  
	  String indexName = version.getAssets();
	  
	  File indexesFolder = new File(baseDirectory, "assets/indexes/");
	  File indexFile = new File(indexesFolder, indexName + ".json");
	  
	  log("Reading indexes from file", indexFile);
	  
	  String json;
	  try { json = FileUtil.readFile(indexFile); } catch (IOException e) {
		  log("Cannot read local resource files list for index:", indexName, e);
		  return null;
	  }
	  
	  AssetIndex index = null;
	  
	  try{ index = this.gson.fromJson(json, AssetIndex.class); }
	  catch(JsonSyntaxException e){ log("JSON file is invalid", e); }
	  
	  if(index == null){
		  log("Cannot read data from JSON file.");
		  return null;
	  }
	  
      for (AssetObject object : index.getUniqueObjects())
    	  result.add(object);
      
	  return result;
  }
  
  private List<AssetObject> getRemoteResourceFilesList(CompleteVersion version, File baseDirectory, boolean save) throws IOException {
	  List<AssetObject> result = new ArrayList<AssetObject>();
	  
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
	  if(save) FileUtil.writeFile(indexFile, json);
	  
      AssetIndex index = this.gson.fromJson(json, AssetIndex.class);
      for (AssetObject object : index.getUniqueObjects())
    	  result.add(object);
      
      return result;
  }
  
  public List<AssetObject> checkResources(CompleteVersion version, File baseDirectory, boolean local, boolean fast){
	  log("Checking resources...");
	  
	  List<AssetObject> list, r = new ArrayList<AssetObject>();
	  
	  if(local) list = this.getLocalResourceFilesList(version, baseDirectory);
	else
		try { list = this.getResourceFilesList(version, baseDirectory, true); }
	    catch (RefreshedException e) { list = this.getLocalResourceFilesList(version, baseDirectory); }
	  
	  if(list == null){
		  log("Cannot get assets list. Aborting.");
		  return r;
	  }
	  
	  log("Fast comparing:", fast);
	  
	  for(AssetObject resource : list)
		  if(!checkResource(baseDirectory, resource, fast))
			  r.add(resource);
	  
	  return r;
			  
	  
	  /*List<AssetObject>
	  	mainList = this.getLocalResourceFilesList(version, baseDirectory),
	  	compareList = null, r = new ArrayList<AssetObject>();
	  
	  if(fast || local) compareList = null;
	  if(!local)
		  try{ compareList = this.getRemoteResourceFilesList(version, baseDirectory, true); }
	  	  catch(IOException e){ log("Cannot get remote assets list.", e); }
	  
	  if(mainList == null && compareList == null){
		  log("Cannot get any information about assets. Aborting.");
		  return r;
	  }
	  
	  if(mainList == null){
		  mainList = compareList;
		  compareList = null;
	  }
	  
	  log("Fast comparing: "+fast);
	  
	  for(AssetObject resource : mainList){
		  
		  if(fast){
			  if(!checkResource(baseDirectory, resource))
				  r.add(resource);
			  continue;
		  }
		  
		  boolean found = false;
		  for(AssetObject compare : compareList)
			  if(resource.getFilename().equalsIgnoreCase( compare.getFilename() )){
				  log(resource.getFilename() + " found in local list");
				  found = true;
				  if(!checkResource(baseDirectory, resource, compare)){
					  log(resource.getFilename() + " was modified on the server - adding.");
					  r.add(resource);
				  }
			  }
		  if(found) continue;
		  
		  log(resource.getFilename() + " isn't found on the local machine - adding.");
		  r.add(resource);
	  }
	  
	  return r;*/
  }
  public List<AssetObject> checkResources(CompleteVersion version){ return checkResources(version, localVersionList.getBaseDirectory(), false, true); }
  
  private boolean checkResource(File baseDirectory, AssetObject local, boolean fast){
	  String path = local.getFilename();
	  
	  File file = new File(baseDirectory, "assets/objects/" + path);
	  if(!file.isFile() || file.length() == 0L) return false;
	  
	  if(fast) return true;
	  
	  String hash = FileUtil.getChecksum(file, "SHA-1");
      return local.getHash() == hash;
  }
  
  public void addRefreshedListener(RefreshedListener listener) {
    this.refreshedListeners.add(listener);
  }

  public void removeRefreshedListener(RefreshedListener listener) {
    this.refreshedListeners.remove(listener);
  }
  
  private void log(Object... w){ U.log("[VersionManager]", w); }
  
  private class RefreshedException extends Exception {
	private static final long serialVersionUID = -614365722177994706L;
  }
}