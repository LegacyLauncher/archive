package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.launcher.LocalLauncher;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.org.apache.commons.io.IOUtils;

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
        Task<LocalLauncher> localLauncherTask = bootstrap.prepareLauncher(null);

        BootBridge bootBridge = BootBridge.create(
                bootstrap.getMeta().getVersion().toString(),
                args,
                IOUtils.toString(BootstrapDebug.class.getResourceAsStream("options.json"), U.UTF8)
        );

        Task<Void> startLauncherTask = bootstrap.startLauncher(localLauncherTask.call(), bootBridge);
        bootstrap.getUserInterface().bindToTask(startLauncherTask);

        startLauncherTask.call();
        bootBridge.waitUntilClose();

        System.exit(0);
    }

}
