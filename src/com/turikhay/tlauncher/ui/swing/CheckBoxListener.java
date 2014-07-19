package com.turikhay.tlauncher.ui.swing;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class CheckBoxListener implements ItemListener {
	@Override
	public void itemStateChanged(ItemEvent e) {
		this.itemStateChanged(e.getStateChange() == ItemEvent.SELECTED);
	}

	public abstract void itemStateChanged(boolean newstate);
}
