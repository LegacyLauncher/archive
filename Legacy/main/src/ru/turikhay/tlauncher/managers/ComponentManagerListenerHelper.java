package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ComponentManagerListenerHelper extends LauncherComponent implements Blockable, VersionManagerListener {
    private final List<ComponentManagerListener> listeners = Collections.synchronizedList(new ArrayList());

    public ComponentManagerListenerHelper(ComponentManager manager) throws Exception {
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
        Blocker.block(this, manager);
    }

    public void onVersionsRefreshingFailed(VersionManager manager) {
        Blocker.unblock(this, manager);
    }

    public void onVersionsRefreshed(VersionManager manager) {
        Blocker.unblock(this, manager);
    }

    public void block(Object reason) {
        Iterator var3 = listeners.iterator();

        while (var3.hasNext()) {
            ComponentManagerListener listener = (ComponentManagerListener) var3.next();
            listener.onComponentsRefreshing(manager);
        }

    }

    public void unblock(Object reason) {
        Iterator var3 = listeners.iterator();

        while (var3.hasNext()) {
            ComponentManagerListener listener = (ComponentManagerListener) var3.next();
            listener.onComponentsRefreshed(manager);
        }

    }
}
