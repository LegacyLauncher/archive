package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.managers.ComponentManager;

/**
 * An abstract <code>LauncherComponent</code> that uses Internet connection or
 * receives information from outside.
 * 
 * @author Artur Khusainov
 * 
 */
public abstract class RefreshableComponent extends LauncherComponent {

	protected RefreshableComponent(ComponentManager manager) throws Exception {
		super(manager);
	}

	public boolean refreshComponent() {
		return refresh();
	}

	protected abstract boolean refresh();
}
