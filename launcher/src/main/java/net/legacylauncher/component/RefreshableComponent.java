package net.legacylauncher.component;

import net.legacylauncher.managers.ComponentManager;
import net.legacylauncher.util.async.AsyncThread;

public abstract class RefreshableComponent extends LauncherComponent {
    public RefreshableComponent(ComponentManager manager) {
        super(manager);
    }

    public boolean refreshComponent() {
        return refresh();
    }

    public void asyncRefresh() {
        AsyncThread.execute(this::refresh);
    }

    protected abstract boolean refresh();
}
