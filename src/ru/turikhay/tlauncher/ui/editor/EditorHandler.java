package ru.turikhay.tlauncher.ui.editor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.turikhay.tlauncher.ui.block.Blockable;

public abstract class EditorHandler implements Blockable {
	private final String path;
	private String value;

	private final List<EditorFieldListener> listeners;

	public EditorHandler(String path) {
		if (path == null)
			throw new NullPointerException();

		this.path = path;
		this.listeners = Collections
				.synchronizedList(new ArrayList<EditorFieldListener>());
	}

	public boolean addListener(EditorFieldListener listener) {
		if (listener == null)
			throw new NullPointerException();

		return listeners.add(listener);
	}

	public boolean removeListener(EditorFieldListener listener) {
		if (listener == null)
			throw new NullPointerException();

		return listeners.remove(listener);
	}

	public void onChange(String newvalue) {
		for (EditorFieldListener listener : listeners)
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
