package ru.turikhay.tlauncher.ui.swing;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public abstract class EmptyAction implements Action {
    protected boolean enabled = true;

    public Object getValue(String key) {
        return null;
    }

    public void putValue(String key, Object value) {
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}
