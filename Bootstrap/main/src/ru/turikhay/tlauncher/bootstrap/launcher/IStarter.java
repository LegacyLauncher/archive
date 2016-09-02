package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.task.Task;

public interface IStarter {
    Task<Void> start(LocalLauncher launcher);
}
