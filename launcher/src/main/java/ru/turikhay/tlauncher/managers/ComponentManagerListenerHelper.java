package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.util.SwingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentManagerListenerHelper extends LauncherComponent implements Blockable, VersionManagerListener {
    private final List<ComponentManagerListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public ComponentManagerListenerHelper(ComponentManager manager) {
        super(manager);
        manager.getComponent(VersionManager.class).addListener(this);
    }

    public void addListener(ComponentManagerListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            listeners.add(listener);
        }
    }

    public void onVersionsRefreshing(VersionManager manager) {
        SwingUtil.later(() -> Blocker.block(this, manager));
    }

    public void onVersionsRefreshingFailed(VersionManager manager) {
        SwingUtil.later(() -> Blocker.unblock(this, manager));
    }

    public void onVersionsRefreshed(VersionManager manager) {
        SwingUtil.later(() -> Blocker.unblock(this, manager));
    }

    public void block(Object reason) {

        for (ComponentManagerListener listener : listeners) {
            SwingUtil.later(() -> listener.onComponentsRefreshing(manager));
        }

    }

    public void unblock(Object reason) {

        for (ComponentManagerListener listener : listeners) {
            SwingUtil.later(() -> listener.onComponentsRefreshed(manager));
        }

    }
}
