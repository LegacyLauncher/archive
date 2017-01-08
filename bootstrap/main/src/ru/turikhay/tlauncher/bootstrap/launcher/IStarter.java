package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.bridge.BootException;
import ru.turikhay.tlauncher.bootstrap.task.Task;

import java.io.File;

public interface IStarter {
    Task<Void> start(LocalLauncher launcher, BootBridge bridge);
}
