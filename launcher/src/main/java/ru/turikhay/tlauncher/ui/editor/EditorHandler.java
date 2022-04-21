package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.block.Blockable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class EditorHandler implements Blockable {
    private String path;
    private String value;
    private final List<EditorFieldListener> listeners;

    public EditorHandler(String path) {
        this.path = path;
        listeners = Collections.synchronizedList(new ArrayList<>());
    }

    public boolean addListener(EditorFieldListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            return listeners.add(listener);
        }
    }

    public boolean removeListener(EditorFieldListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            return listeners.remove(listener);
        }
    }

    public void onChange(String newvalue) {
        if (TLauncher.getInstance().isReady()) {

            for (EditorFieldListener listener : listeners) {
                listener.onChange(this, value, newvalue);
            }
        }

        value = newvalue;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void updateValue(Object obj) {
        String val = obj == null ? null : obj.toString();
        onChange(val);
        setValue0(value);
    }

    public void setValue(Object obj) {
        String val = obj == null ? null : obj.toString();
        setValue0(val);
    }

    public abstract boolean isValid();

    public abstract JComponent getComponent();

    public abstract String getValue();

    protected abstract void setValue0(String var1);

    public String toString() {
        return getClass().getSimpleName() + "{path='" + path + "', value='" + value + "'}";
    }
}
