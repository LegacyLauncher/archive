package com.turikhay.tlauncher.ui.settings;

import java.awt.Component;

import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.swing.extended.VPanel;

class SettingsPair {
	private final LocalizableLabel label;
	private final SettingsHandler[] handlers;
	private final Component[] fields;
	private final VPanel panel;

	SettingsPair(String labelPath, SettingsHandler... handlers) {
		this.label = new LocalizableLabel(labelPath);

		int num = handlers.length;

		this.fields = new Component[num];

		for (int i = 0; i < num; i++)
			this.fields[i] = handlers[i].getComponent();

		this.handlers = handlers;

		this.panel = new VPanel();
		this.panel.add(fields);
	}

	public SettingsHandler[] getHandlers() {
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
