package ru.turikhay.tlauncher.ui.swing;

import java.beans.PropertyChangeListener;

import javax.swing.Action;

public abstract class EmptyAction implements Action {
	protected boolean enabled;
	
	public EmptyAction() {
		this.enabled = true;
	}

	@Override
	public Object getValue(String key) {
		return null;
	}

	@Override
	public void putValue(String key, Object value) {
	}

	@Override
	public void setEnabled(boolean b) {
		this.enabled = b;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

}
