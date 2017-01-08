package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.task.Task;

import java.io.File;
import java.io.FileNotFoundException;

public class BootstrapDebug {

    public static void main(String[] args) throws Exception {
        File launcherFile = new File("out/production/launcher");
        if(!launcherFile.isDirectory()) {
            throw new FileNotFoundException("launcher: " + launcherFile.getAbsolutePath());
        }

        File libDir = new File("lib/repo");
        if(!libDir.isDirectory()) {
            throw new FileNotFoundException("libDir: " + libDir.getAbsolutePath());
        }

        Bootstrap bootstrap = new Bootstrap(launcherFile, libDir);

        Task<BootBridge> bridgeTask = bootstrap.bootLauncher(null, args);
        bootstrap.getUserInterface().bindToTask(bridgeTask);

        BootBridge bootBridge = bridgeTask.call();
        bootBridge.waitUntilClose();

        System.exit(0);
    }

}
