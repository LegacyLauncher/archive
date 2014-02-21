package com.turikhay.tlauncher.component.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionList.RawVersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.updater.VersionFilter;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.InterruptibleComponent;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.minecraft.repository.VersionRepository;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncObject;
import com.turikhay.util.async.AsyncObjectContainer;
import com.turikhay.util.async.AsyncThread;

public class VersionManager extends InterruptibleComponent {
	
	private final LocalVersionList localList;
	private final RemoteVersionList[] remoteLists;
	
	private final List<VersionManagerListener> listeners;
	private final Object versionFlushLock;
	
	public VersionManager(LocalVersionList local, RemoteVersionList... lists) {
		if(local == null)
			throw new NullPointerException("LocalVersionList is NULL!");
		
		if(lists == null)
			throw new NullPointerException("RemoteVersionList array is NULL!");
		
		this.localList = local;
		this.remoteLists = lists;
		
		this.listeners = Collections.synchronizedList(new ArrayList<VersionManagerListener>());
		this.versionFlushLock = new Object();
	}
	
	public VersionManager(ComponentManager manager){
		this(manager.getVersionLists().getLocal(), manager.getVersionLists().getRemoteLists());
	}
	
	public void addListener(VersionManagerListener listener){
		if(listener == null)
			throw new NullPointerException();
		
		this.listeners.add(listener);
	}
	
	public LocalVersionList getLocalList(){
		return localList;
	}
	
	protected boolean refresh(int refreshID, boolean local) {
		this.refreshList[refreshID] = true;
		
		log("Refreshing versions...");
		
		for(VersionManagerListener listener : listeners)
			listener.onVersionsRefreshing(this);
		
		Object lock = new Object();
		Time.start(lock);
		
		Map<AsyncObject<RawVersionList>, RawVersionList> result = null;
		Throwable e = null;
		
		try {
			result = refreshVersions(local);
		}catch(Throwable e0){
			e = e0;
		}
		
		if(isCancelled(refreshID)){
			log("Version refresh has been cancelled ("+Time.stop(lock)+" ms)");
			return false;
		}
		
		if(e != null){
			for(VersionManagerListener listener : listeners)
				listener.onVersionsRefreshingFailed(this);
		
			log("Cannot refresh versions ("+Time.stop(lock)+" ms)", e);
			return true;
		}
		
		if(result != null)
			synchronized(versionFlushLock){
				for(AsyncObject<RawVersionList> object : result.keySet()) {
					RawVersionList rawList = result.get(object);
					if(rawList == null) continue;
				
					AsyncRawVersionListObject listObject = (AsyncRawVersionListObject) object;
					listObject.getVersionList().refreshVersions(rawList);
				}
			}
		
		log("Versions has been refreshed ("+Time.stop(lock)+" ms)");
		
		this.refreshList[refreshID] = false;
		
		for(VersionManagerListener listener : listeners)
			listener.onVersionsRefreshed(this);
		
		return true;
	}

	@Override
	protected boolean refresh(int queueID) {
		return refresh(queueID, false);
	}
	
	public void startRefresh(boolean local) {
		this.refresh(nextID(), local);
	}
	
	@Override
	public synchronized void stopRefresh() {
		super.stopRefresh();
		this.startRefresh(true);
	}
	
	public void asyncRefresh(final boolean local){
		AsyncThread.execute(new Runnable(){
			@Override
			public void run() {
				startRefresh(local);
			}
		});
	}
	
	public void asyncRefresh(){
		asyncRefresh(false);
	}
	
	private Map<AsyncObject<RawVersionList>, RawVersionList> refreshVersions(boolean local) throws IOException {
		this.localList.refreshVersions();
		
		if(local) return null;
		
		AsyncObjectContainer<RawVersionList> container = new AsyncObjectContainer<RawVersionList>();
		
		for(RemoteVersionList remoteList : remoteLists)			
			container.add(new AsyncRawVersionListObject(remoteList));
		
		return container.execute();
	}
	
	public void updateVersionList() {
		for(VersionManagerListener listener : listeners)
			listener.onVersionsRefreshed(this);
	}
	
	public VersionSyncInfo getVersionSyncInfo(Version version) {
		return getVersionSyncInfo(version.getID());
	}

	public VersionSyncInfo getVersionSyncInfo(String name) {
		if(name == null)
			throw new NullPointerException("Cannot get sync info of NULL!");
		
		Version localVersion = localList.getVersion(name);
		Version remoteVersion = null;
		
		for(RemoteVersionList list : remoteLists){
			Version currentVersion = list.getVersion(name);
			if(currentVersion == null) continue;
			
			remoteVersion = currentVersion;
			break;
		}
		
		// TODO may be return null if all versions are null?
		return new VersionSyncInfo(localVersion, remoteVersion);
	}
	
	public List<VersionSyncInfo> getVersions(VersionFilter filter) {
		synchronized(versionFlushLock){
			return getVersions0(filter);
		}
	}
	
	public List<VersionSyncInfo> getVersions() {
		return getVersions(TLauncher.getInstance() == null ? null : TLauncher.getInstance().getSettings().getVersionFilter());
	}
	
	private List<VersionSyncInfo> getVersions0(VersionFilter filter) {
		if(filter == null) filter = new VersionFilter();

		List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();
		Map<String, VersionSyncInfo> lookup = new HashMap<String, VersionSyncInfo>();
		
		for(Version version : localList.getVersions()) {
			if(!filter.satisfies(version)) continue;
			
			VersionSyncInfo syncInfo = getVersionSyncInfo(version);
			lookup.put(version.getID(), syncInfo);
			result.add(syncInfo);
		}
		
		for(RemoteVersionList remoteList : remoteLists)
			for(Version version : remoteList.getVersions()) {
				if(lookup.containsKey(version.getID()) || !filter.satisfies(version)) continue;
				
				VersionSyncInfo syncInfo = getVersionSyncInfo(version);
				lookup.put(version.getID(), syncInfo);
				result.add(syncInfo);
			}

		Collections.sort(result, new Comparator<VersionSyncInfo>() {
			public int compare(VersionSyncInfo a, VersionSyncInfo b) {
				Version aVer = a.getLatestVersion();
				Version bVer = b.getLatestVersion();
				
				// TODO remove null check?
				if(aVer.getReleaseTime() != null && bVer.getReleaseTime() != null)
					return bVer.getReleaseTime().compareTo(aVer.getReleaseTime());
				
				return bVer.getUpdatedTime().compareTo(aVer.getUpdatedTime());
			}
		});
		
		return result;
	}
	
	public List<VersionSyncInfo> getInstalledVersions(VersionFilter filter) {
		if(filter == null) filter = new VersionFilter();
		List<VersionSyncInfo> result = new ArrayList<VersionSyncInfo>();
		
		for(Version version : localList.getVersions())
			result.add(getVersionSyncInfo(version));
		
		return result;
	}
	
	public List<VersionSyncInfo> getInstalledVersions() {
		return getInstalledVersions(null);
	}
	
	public CompleteVersion getCompleteVersion(VersionSyncInfo syncInfo, boolean latest) throws IOException {
		Version version;
		
		if(latest) version = syncInfo.getLatestVersion();
		else if(syncInfo.isInstalled()) version = syncInfo.getLocal();
		else version = syncInfo.getRemote();
			
		return version.getVersionList().getCompleteVersion(version);
	}
	
	public CompleteVersion getLatestCompleteVersion(VersionSyncInfo syncInfo) throws IOException {
		return getCompleteVersion(syncInfo, true);
	}
	
	public void downloadVersion(DownloadableContainer job, VersionSyncInfo syncInfo, boolean force) throws IOException {
		log("Downaloding version:", syncInfo);
		CompleteVersion version = getCompleteVersion(syncInfo, force);
		log("Complete:", version);
		File baseDirectory = localList.getBaseDirectory();
		
		job.addAll(version.getRequiredDownloadables(baseDirectory, force));
		
		if(!syncInfo.hasRemote()) return;
		
	    String
	        id = version.getID(), o_id = version.getOriginal(),
	        downloadPath, jarFile = "versions/", saveFile = jarFile;
	        
	    if(o_id != null){
	    	// Original versions should be downloaded from the default repo
	    	downloadPath = VersionRepository.OFFICIAL.getSelectedRepo();
	    	jarFile += o_id + "/" + o_id + ".jar";
	    	saveFile += id + "/" + id + ".jar";
	    } else {
	    	downloadPath = syncInfo.getLatestVersion().getSource().getSelectedRepo();
	    	jarFile += id + "/" + id + ".jar";
	    	saveFile = jarFile;
	    }
	    
    	// TODO set repository to downloadable
	    Downloadable d = new Downloadable(downloadPath + jarFile, new File(baseDirectory, saveFile), force);
	    d.setAdditionalDestinations(new File[]{ new File(d.getDestination() + ".bak") });
	        
	    job.add(d);
	}
	
	protected void log(Object... w){ U.log("["+getClass().getSimpleName()+"]", w); }
	
	class AsyncRawVersionListObject extends AsyncObject<RawVersionList> {
		private final RemoteVersionList remoteList;
		
		AsyncRawVersionListObject(RemoteVersionList remoteList){
			this.remoteList = remoteList;
		}
		
		protected RemoteVersionList getVersionList(){
			return remoteList;
		}

		@Override
		protected RawVersionList execute() {
  			try {
				return remoteList.getRawList();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
