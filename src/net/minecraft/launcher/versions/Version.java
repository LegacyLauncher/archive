package net.minecraft.launcher.versions;

import java.util.Date;

import net.minecraft.launcher.updater.VersionList;

import com.turikhay.tlauncher.minecraft.repository.VersionRepository;

public interface Version {
	public String getID();
	public void setID(String id);
	
	public ReleaseType getReleaseType();
	
	public VersionRepository getSource();
	public void setSource(VersionRepository repository);
	
	public Date getUpdatedTime();
	public Date getReleaseTime();
	
	public VersionList getVersionList();
	public void setVersionList(VersionList list);
}
