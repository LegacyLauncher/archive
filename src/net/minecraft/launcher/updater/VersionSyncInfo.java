package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.Version;

public class VersionSyncInfo {
	private final Version localVersion, remoteVersion;
	
	public VersionSyncInfo(Version localVersion, Version remoteVersion){
		if(localVersion == null && remoteVersion == null)
			throw new NullPointerException("Cannot create sync info from NULLs!");
		
		this.localVersion = localVersion;
		this.remoteVersion = remoteVersion;
		
		if(getID() == null)
			throw new NullPointerException("Cannot create sync info from versions that have NULL IDs");
	}
	
	private VersionSyncInfo() {
		this.localVersion = null;
		this.remoteVersion = null;
	}
	
	public Version getLocal(){
		return localVersion;
	}
	
	public Version getRemote(){
		return remoteVersion;
	}
	
	public String getID(){
		if(localVersion != null)
			return localVersion.getID();
		
		if(remoteVersion != null)
			return remoteVersion.getID();
		
		return null;
	}
	
	public Version getLatestVersion(){
		if(remoteVersion != null)
			return remoteVersion;
		
		return localVersion;
	}
	
	public boolean isInstalled(){
		return localVersion != null;
	}
	
	public boolean hasRemote(){
		return remoteVersion != null;
	}
	
	public boolean isUpToDate(){
		if(localVersion == null) return false;
		if(remoteVersion == null) return true;
		
		return localVersion.getReleaseTime().compareTo(remoteVersion.getReleaseTime()) >= 0;
	}
	
	public String toString(){
		return "VersionSyncInfo{id='"+getID()+"',\nlocal="+localVersion+",\nremote="+remoteVersion+", isInstalled="+isInstalled()+", hasRemote="+hasRemote()+", isUpToDate="+isUpToDate()+"}";
	}
	
	public static VersionSyncInfo createEmpty(){
		return new VersionSyncInfo();
	}
}
