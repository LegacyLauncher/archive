package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.task.Task;

import java.io.File;

public abstract class LocalCastingLauncher extends Launcher {
    public abstract Task<LocalLauncher> toLocalLauncher(File file, File libFolder);
}
