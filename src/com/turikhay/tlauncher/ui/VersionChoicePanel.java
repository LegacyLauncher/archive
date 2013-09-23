package com.turikhay.tlauncher.ui;

import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

import net.minecraft.launcher_.events.RefreshedListener;
import net.minecraft.launcher_.updater.VersionFilter;
import net.minecraft.launcher_.updater.VersionManager;
import net.minecraft.launcher_.updater.VersionSyncInfo;
import net.minecraft.launcher_.versions.Version;

public class VersionChoicePanel extends BlockablePanel implements RefreshedListener, LocalizableComponent {
	private static final long serialVersionUID = -1838948772565245249L;
	
	private final LoginForm lf;
	private final Settings l;
	private final VersionManager vm;
	
	String version;
	VersionSyncInfo selected;
	
	Map<String, String> list;
	List<VersionSyncInfo> lastupdate = new ArrayList<VersionSyncInfo>();
	Choice choice;
	boolean foundlocal;
	
	VersionChoicePanel(LoginForm lf, String ver){
		this.lf = lf; this.l = lf.l; this.vm = lf.t.getVersionManager();
		this.version = ver;
		
		LayoutManager lm = new GridLayout(1, 1);
		this.setLayout(lm);
		
		this.choice = new Choice();
		this.choice.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				version = list.get(e.getItem().toString());
				onVersionChanged();
			}
		});
		this.list = new LinkedHashMap<String, String>();
		
		this.add(choice);
	}
	
	void onVersionChanged(){
		foundlocal = true;
		
		selected = vm.getVersionSyncInfo(version);		
		lf.buttons.updateEnterButton();

		this.unblock("refresh");
	}
	
	void refreshVersions(VersionManager vm, boolean local){ lf.unblock("version_refresh");
		list.clear();
		
		VersionFilter vf = MinecraftUtil.getVersionFilter();
		lastupdate = (local)? vm.getInstalledVersions(vf) : vm.getVersions(vf);
		
		this.updateLocale();
	}
	protected void blockElement(Object reason) {
		choice.setEnabled(false);
	}
	protected void unblockElement(Object reason) {
		choice.setEnabled(true);
	}
	public void onVersionsRefreshed(VersionManager vm) { refreshVersions(vm, false); }
	public void onVersionsRefreshingFailed(VersionManager vm){ refreshVersions(vm, true); }
	public void onVersionsRefreshing(VersionManager vm){
		list.clear(); choice.setEnabled(false); choice.removeAll();
		choice.add(l.get("versions.loading"));
		
		lf.block("version_refresh");
	}
	public void refresh(){ vm.refreshVersions(); }
	public void asyncRefresh(){ vm.asyncRefresh(); }
	public VersionSyncInfo getSyncVersionInfo(){ return selected; }

	public void onResourcesRefreshing(VersionManager vm) {
		lf.block("resource_refresh");
	}

	public void onResourcesRefreshed(VersionManager vm) {
		lf.unblock("resource_refresh");
	}

	public void handleUpdate(boolean ok) {
		VersionSyncInfo syncInfo;
		
		if(!ok){
			syncInfo = this.getSyncVersionInfo();
			syncInfo.getLocalVersion().setUpdatedTime(new Date());
		} else {
			syncInfo = vm.getVersionSyncInfo(version);
		}
		try {
			vm.getLocalVersionList().saveVersion(vm.getLatestCompleteVersion(syncInfo));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateLocale(){
		choice.removeAll();
		
		boolean exists = false; String add = "";
		for(VersionSyncInfo curv : lastupdate){
			Version ver = curv.getLatestVersion();
			String id = ver.getId(), dId = id;
			
			if(id.length() < 18)
				switch(ver.getType()){
				case OLD_ALPHA:
					dId = l.get("version.alpha", "v", (id.startsWith("a"))? id.substring(1) : id);
					break;
				case OLD_BETA:
					dId = l.get("version.beta", "v", id.substring(1));
					break;
				case RELEASE:
					dId = l.get("version.release", "v", id);
					break;
				case SNAPSHOT:
					dId = l.get("version.snapshot", "v", id);
					break;
				default:
					break;
				}
			else {
				if(add.length() > 2) add = ""; add += "~";
				dId = U.t(id, 16) + add;
			}
			
			choice.add(dId); list.put(dId, id);
			if(id.equals(version)){
				version = id;
				choice.select(dId);
				onVersionChanged();
				
				exists = true;
			}
		}
		
		if(choice.getItemCount() > 0){
			if(!exists || version == null) version = list.get(choice.getItem(0));
			onVersionChanged();
			return;
		}
		choice.add(l.get("versions.notfound.tip"));
	}
}
