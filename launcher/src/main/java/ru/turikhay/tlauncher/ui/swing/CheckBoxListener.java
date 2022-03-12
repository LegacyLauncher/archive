package ru.turikhay.tlauncher.ui.swing;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class CheckBoxListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
        itemStateChanged(e.getStateChange() == ItemEvent.SELECTED);
    }

    public abstract void itemStateChanged(boolean var1);
}
