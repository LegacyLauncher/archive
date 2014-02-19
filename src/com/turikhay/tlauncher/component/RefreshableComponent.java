package com.turikhay.tlauncher.component;

/**
 * An abstract <code>LauncherComponent</code> that uses Internet connection or receives information
 * from outside.
 * 
 * @author Artur Khusainov
 *
 */
public abstract class RefreshableComponent implements LauncherComponent {
	
	public boolean refreshComponent() {
		return refresh();
	}
	
	protected abstract boolean refresh();
}
