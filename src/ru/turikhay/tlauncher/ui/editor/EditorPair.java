package ru.turikhay.tlauncher.ui.editor;

import java.awt.Component;

import javax.swing.JComponent;

import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;

public class EditorPair {
	private final LocalizableLabel label;
	private final EditorHandler[] handlers;
	private final JComponent[] fields;
	private final VPanel panel;

	public EditorPair(String labelPath, EditorHandler... handlers) {
		this.label = new LocalizableLabel(labelPath);

		int num = handlers.length;

		this.fields = new JComponent[num];

		for (int i = 0; i < num; i++) {
			this.fields[i] = handlers[i].getComponent();
			this.fields[i].setAlignmentX(0);
		}

		this.handlers = handlers;

		this.panel = new VPanel();
		this.panel.add(fields);
	}

	public EditorHandler[] getHandlers() {
		return handlers;
	}

	public LocalizableLabel getLabel() {
		return label;
	}

	public Component[] getFields() {
		return fields;
	}

	public VPanel getPanel() {
		return panel;
	}
}
