package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.util.U;

public final class LocalLauncherTask {
    private final LocalLauncher launcher;
    private boolean updated;

    public LocalLauncherTask(LocalLauncher launcher, boolean updated) {
        this.launcher = U.requireNotNull(launcher, "launcher");
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

    void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
