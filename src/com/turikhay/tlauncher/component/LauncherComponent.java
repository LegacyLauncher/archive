package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.managers.ComponentManager;
import com.turikhay.util.U;

/**
 * An abstract interface to define components.
 * 
 * @author Artur Khusainov
 * 
 */
public abstract class LauncherComponent {
	protected final ComponentManager manager;

	public LauncherComponent(ComponentManager manager) throws Exception {
		if (manager == null)
			throw new NullPointerException();

		this.manager = manager;
	}

	protected void log(Object... w) {
		U.log("[" + getClass().getSimpleName() + "]", w);
	}
}
