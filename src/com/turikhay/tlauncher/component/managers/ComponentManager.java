package com.turikhay.tlauncher.component.managers;

import com.turikhay.tlauncher.component.*;
import com.turikhay.util.async.AsyncThread;

public class ComponentManager {
	private final VersionLists versionLists;
	private final VersionManager versionManager;
	
	private final AssetsManager assetsManager;
	
	private final ProfileManager profileManager;
	
	private final ComponentManagerListenerHelper listenerHelper;
	private final LauncherComponent[] components;
	
	public ComponentManager() throws Exception {
		this.versionLists = new VersionLists();
		
		this.versionManager = new VersionManager(this);
		
		this.assetsManager = new AssetsManager(this);
		
		this.profileManager = new ProfileManager();
		
		this.listenerHelper = new ComponentManagerListenerHelper(this);
		this.components = new LauncherComponent[]{ versionManager, assetsManager, profileManager };
	}
	
	public void addListener(ComponentManagerListener listener){
		listenerHelper.addListener(listener);
	}
	
	public VersionLists getVersionLists(){
		return versionLists;
	}
	
	public VersionManager getVersionManager(){
		return versionManager;
	}
	
	public AssetsManager getAssetsManager(){
		return assetsManager;
	}
	
	public ProfileManager getProfileManager(){
		return profileManager;
	}
	
	public ServerListManager getServerListManager(){
		return null;
	}
	
	public LauncherComponent[] getComponents(){
		return components;
	}
	
	public void startAsyncRefresh(){
		for(LauncherComponent component : components)
			if(component instanceof RefreshableComponent){
				final RefreshableComponent interruptible = (RefreshableComponent) component;
				AsyncThread.execute(new Runnable(){
					@Override
					public void run() {
						interruptible.refreshComponent();
					}
				});
			}
	}
	
	public void startRefresh(){
		for(LauncherComponent component : components)
			if(component instanceof RefreshableComponent){
				RefreshableComponent interruptible = (RefreshableComponent) component;
				interruptible.refreshComponent();
			}
	}
	
	public void stopRefresh(){
		for(LauncherComponent component : components)
			if(component instanceof InterruptibleComponent){
				InterruptibleComponent interruptible = (InterruptibleComponent) component;
				interruptible.stopRefresh();
			}
	}
	
	public boolean contains(Class<?> componentClass) {
		if(componentClass == null)
			throw new NullPointerException();
		
		for(LauncherComponent component : components)
			if(component.getClass().equals(componentClass))
				return true;
		
		return false;
	}
}
