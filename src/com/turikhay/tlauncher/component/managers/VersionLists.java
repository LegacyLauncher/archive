package com.turikhay.tlauncher.component.managers;

import java.io.IOException;

import com.turikhay.util.MinecraftUtil;

import net.minecraft.launcher.updater.ExtraVersionList;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.OfficialVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;

public class VersionLists {
	private final LocalVersionList localList;
	
	private final OfficialVersionList officialList;
	private final ExtraVersionList extraList;
	
	private final RemoteVersionList[] remoteLists;
	
	public VersionLists() throws IOException {
		this.localList = new LocalVersionList(MinecraftUtil.getWorkingDirectory());
		
		this.officialList = new OfficialVersionList();
		this.extraList = new ExtraVersionList();
		
		this.remoteLists = new RemoteVersionList[]{ officialList, extraList };
	}
	
	public LocalVersionList getLocal(){
		return localList;
	}
	
	public void updateLocal() throws IOException {
		this.localList.setBaseDirectory(MinecraftUtil.getWorkingDirectory());
	}
	
	public OfficialVersionList getOfficial(){
		return officialList;
	}
	
	public RemoteVersionList[] getRemoteLists(){
		return remoteLists;
	}
}
