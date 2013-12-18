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
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;

import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;

public class VersionChoicePanel extends BlockablePanel implements RefreshedListener, LocalizableComponent, LoginListener {
	private static final long serialVersionUID = -1838948772565245249L;
	
	private final LoginForm lf;
	private final Settings l;
	private VersionManager vm;
	
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
		this.setOpaque(false);
		
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
		U.log("Selected", version);
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
		choice.removeAll(); list.clear();
		
		boolean exists = false; String add = "";
		for(VersionSyncInfo curv : lastupdate){
			Version ver = curv.getLatestVersion();
			String id = ver.getId(), dId = id;
			
			if(id.length() < 19)
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
				dId = U.t(id, 20);
				if(dId.length() != id.length()){
					if(add.length() > 2) add = ""; add += "~";
					dId += add; 
				}
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
			if(exists && version != null) return;
			
			int select = 0;
			
			for(int i=0;i<choice.getItemCount();i++){
				String ch = list.get(choice.getItem(i));
				VersionSyncInfo vs = vm.getVersionSyncInfo(ch);
				
				if(!vs.getLatestVersion().getType().isDesired())
					continue;
				
				select = i;
				break;
			}
			
			version = list.get(choice.getItem(select));
			choice.select(select);
			
			onVersionChanged();
			return;
		}
		foundlocal = false;
		choice.add(l.get("versions.notfound.tip"));
	}

	public void onVersionManagerUpdated(VersionManager vm) { vm.asyncRefresh(); }

	public boolean onLogin() {
		if(!foundlocal){
			refresh();
			
			if(!foundlocal) Alert.showError("versions.notfound");
			return false;
		}
		
		if(!selected.isInstalled() || selected.isUpToDate()) return true;
		
		if(!Alert.showQuestion("versions.found-update", false)){
			try {
				CompleteVersion complete = vm.getLocalVersionList().getCompleteVersion(version);				
				complete.setUpdatedTime(selected.getLatestVersion().getUpdatedTime());
				
				vm.getLocalVersionList().saveVersion(complete);
			} catch(IOException e){
				Alert.showError("versions.found-update.error");
			}
			return true;
		}
		
		lf.checkbox.setForceUpdate(true);
		return true;
	}
	public void onLoginFailed() {}
	public void onLoginSuccess() {} 
}

/*package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

import net.minecraft.launcher_.events.RefreshedListener;
import net.minecraft.launcher_.updater.VersionFilter;
import net.minecraft.launcher_.updater.VersionManager;
import net.minecraft.launcher_.updater.VersionSyncInfo;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;

public class VersionChoicePanel extends BlockablePanel implements RefreshedListener, LocalizableComponent, LoginListener {
	private static final long serialVersionUID = -1838948772565245249L;
	
	private final LoginForm lf;
	private final Settings l;
	private VersionManager vm;
	
	String version;
	VersionSyncInfo selected;
	
	Map<String, String> list;
	List<VersionSyncInfo> lastupdate = new ArrayList<VersionSyncInfo>();
	JComboBox box;
	boolean foundlocal;
	
	VersionChoicePanel(LoginForm lf, String ver){
		this.lf = lf; this.l = lf.l; this.vm = lf.t.getVersionManager();
		this.version = ver;
		
		LayoutManager lm = new GridLayout(1, 1);
		this.setLayout(lm);
		this.setOpaque(false);
		
		this.box = new JComboBox(); this.box.setOpaque(false); this.box.setBackground(new Color(0, 0, 0, 0));
		this.box.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				String value = toString(e);
				if(value == null) return;
				
				String selected = list.get(value);
				if(selected == null) return;
				
				version = selected;
				onVersionChanged();
			}
			
			private String toString(ItemEvent e) {
				ItemSelectable is = e.getItemSelectable();
				Object selected[] = is.getSelectedObjects();
				return ((selected.length == 0) ? null : (String) selected[0]);
			}
		});
		this.list = new LinkedHashMap<String, String>();
		
		this.add(box);
	}
	
	void onVersionChanged(){
		U.log("Selected", version);
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
		box.setEnabled(false);
	}
	protected void unblockElement(Object reason) {
		box.setEnabled(true);
	}
	public void onVersionsRefreshed(VersionManager vm) { refreshVersions(vm, false); }
	public void onVersionsRefreshingFailed(VersionManager vm){ refreshVersions(vm, true); }
	public void onVersionsRefreshing(VersionManager vm){
		list.clear(); box.setEnabled(false); box.removeAllItems();
		box.addItem(l.get("versions.loading"));
		
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
		box.removeAllItems(); list.clear();
		
		boolean exists = false; String add = "";
		for(VersionSyncInfo curv : lastupdate){
			Version ver = curv.getLatestVersion();
			String id = ver.getId(), dId = id;
			
			if(id.length() < 19)
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
				dId = U.t(id, 20);
				if(dId.length() != id.length()){
					if(add.length() > 2) add = ""; add += "~";
					dId += add; 
				}
			}
			
			box.addItem(dId); list.put(dId, id);
			if(id.equals(version)){
				version = id;
				box.getModel().setSelectedItem(dId);
				onVersionChanged();
				
				exists = true;
			}
		}
		
		if(exists && version != null) return;
		if(box.getItemCount() > 0){			
			int select = 0;
			
			for(int i=0;i<box.getItemCount();i++){
				String ch = list.get(box.getItemAt(i));
				VersionSyncInfo vs = vm.getVersionSyncInfo(ch);
				
				if(vs.getLatestVersion().getType() == ReleaseType.CHEAT)
					continue;
				
				select = i;
				break;
			}
			
			version = list.get(box.getItemAt(select));
			box.setSelectedIndex(select);
			
			onVersionChanged();
			return;
		}
		foundlocal = false;
		box.addItem(l.get("versions.notfound.tip"));
	}

	public void onVersionManagerUpdated(VersionManager vm) { vm.asyncRefresh(); vm.asyncRefreshResources(); }

	public boolean onLogin() {
		if(foundlocal) return true;
		
		refresh();
		if(foundlocal) return true;
		
		Alert.showError("versions.notfound");
		return false;
	}
	public void onLoginFailed() {}
	public void onLoginSuccess() {}
}
*/
