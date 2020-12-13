package ru.turikhay.tlauncher.component;

import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.util.U;

public abstract class LauncherComponent {
    protected final ComponentManager manager;

    public LauncherComponent(ComponentManager manager) throws Exception {
        if (manager == null) {
            throw new NullPointerException();
        } else {
            this.manager = manager;
        }
    }
}
