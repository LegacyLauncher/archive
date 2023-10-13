package net.legacylauncher.bootstrap.launcher;

import net.legacylauncher.bootstrap.meta.LocalLauncherMeta;
import net.legacylauncher.bootstrap.meta.OldLauncherMeta;

public class InternalLauncherMeta extends LocalLauncherMeta {
    public InternalLauncherMeta() {
    }

    public InternalLauncherMeta(OldLauncherMeta old) {
        super(old);
    }
}
