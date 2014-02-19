package com.turikhay.tlauncher.component.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;

public class ComponentManagerListenerHelper implements Blockable, VersionManagerListener {
	private final ComponentManager manager;
	private final List<ComponentManagerListener> listeners;
	
	ComponentManagerListenerHelper(ComponentManager manager) {
		this.manager = manager;		
		this.listeners = Collections.synchronizedList(new ArrayList<ComponentManagerListener>());
		
		manager.getVersionManager().addListener(this);
	}
	
	public void addListener(ComponentManagerListener listener) {
		if(listener == null)
			throw new NullPointerException();
		
		this.listeners.add(listener);
	}

	@Override
	public void onVersionsRefreshing(VersionManager manager) {
		Blocker.block(this, manager);
	}

	@Override
	public void onVersionsRefreshingFailed(VersionManager manager) {
		Blocker.unblock(this, manager);
	}

	@Override
	public void onVersionsRefreshed(VersionManager manager) {
		Blocker.unblock(this, manager);
	}
	
	//

	@Override
	public void block(Object reason) {
		for(ComponentManagerListener listener : listeners)
			listener.onComponentsRefreshing(manager);
	}

	@Override
	public void unblock(Object reason) {
		for(ComponentManagerListener listener : listeners)
			listener.onComponentsRefreshed(manager);
	}

}
