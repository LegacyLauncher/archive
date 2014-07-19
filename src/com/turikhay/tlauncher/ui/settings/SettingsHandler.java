package com.turikhay.tlauncher.ui.settings;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.turikhay.tlauncher.ui.block.Blockable;

public abstract class SettingsHandler implements Blockable {
	private final String path;
	private String value;

	private final List<SettingsFieldListener> listeners;

	SettingsHandler(String path) {
		if (path == null)
			throw new NullPointerException();

		this.path = path;
		this.listeners = Collections
				.synchronizedList(new ArrayList<SettingsFieldListener>());
	}

	public boolean addListener(SettingsFieldListener listener) {
		if (listener == null)
			throw new NullPointerException();

		return listeners.add(listener);
	}

	public boolean removeListener(SettingsFieldListener listener) {
		if (listener == null)
			throw new NullPointerException();

		return listeners.remove(listener);
	}

	void onChange(String newvalue) {
		for (SettingsFieldListener listener : listeners)
			listener.onChange(this, value, newvalue);

		this.value = newvalue;
	}

	public String getPath() {
		return path;
	}

	public void updateValue(Object obj) {
		String val = (obj == null) ? null : obj.toString();

		this.onChange(val);
		this.setValue0(value);
	}

	public void setValue(Object obj) {
		String val = (obj == null) ? null : obj.toString();

		this.setValue0(val);
	}

	public abstract boolean isValid();

	public abstract Component getComponent();

	public abstract String getValue();

	protected abstract void setValue0(String s);

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{path='" + path + "', value='"
				+ value + "'}";
	}
}