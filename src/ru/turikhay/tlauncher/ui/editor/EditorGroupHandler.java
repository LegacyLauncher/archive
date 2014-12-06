package ru.turikhay.tlauncher.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorGroupHandler {
	private final List<EditorFieldChangeListener> listeners;

	private final int checkedLimit;
	private int changedFlag, checkedFlag;

	public EditorGroupHandler(EditorHandler... handlers) {
		if (handlers == null)
			throw new NullPointerException();

		this.checkedLimit = handlers.length;

		EditorFieldListener listener = new EditorFieldListener() {
			@Override
			protected void onChange(EditorHandler handler, String oldValue,
					String newValue) {
				if (newValue == null)
					return;
				if (!newValue.equals(oldValue))
					++changedFlag;

				++checkedFlag;

				if (checkedFlag == checkedLimit) {
					
					if (changedFlag > 0)
						for (EditorFieldChangeListener listener : listeners)
							listener.onChange(null, null);

					checkedFlag = changedFlag = 0;
				}
			}
		};

		for (int i = 0; i < handlers.length; i++) {
			EditorHandler handler = handlers[i];
			if (handler == null)
				throw new NullPointerException("Handler is NULL at " + i);

			handler.addListener(listener);
		}

		EditorHandler[] handlers1 = new EditorHandler[handlers.length];
		System.arraycopy(handlers, 0, handlers1, 0, handlers.length);

		this.listeners = Collections
				.synchronizedList(new ArrayList<EditorFieldChangeListener>());
	}

	public boolean addListener(EditorFieldChangeListener listener) {
		if (listener == null)
			throw new NullPointerException();

		return listeners.add(listener);
	}

	public boolean removeListener(EditorFieldChangeListener listener) {
		if (listener == null)
			throw new NullPointerException();

		return listeners.remove(listener);
	}
}
