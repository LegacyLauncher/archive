package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.util.U;

public final class LocalLauncherTask {
    private final LocalLauncher launcher;
    private final boolean updated;

    public LocalLauncherTask(LocalLauncher launcher, boolean updated) {
        this.launcher = U.requireNotNull(launcher, "launcher");
        this.updated = updated;
    }

    public LocalLauncherTask(LocalLauncher launcher) {
        this(launcher, false);
    }

    public LocalLauncher getLauncher() {
        return launcher;
    }

    public boolean isUpdated() {
        return updated;
    }
}
