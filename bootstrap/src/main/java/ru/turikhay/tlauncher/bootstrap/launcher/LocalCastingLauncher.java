package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.task.Task;

import java.nio.file.Path;

public abstract class LocalCastingLauncher extends Launcher {
    public abstract Task<LocalLauncher> toLocalLauncher(Path file, Path libFolder);
}
