package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.launcher.LocalLauncher;
import ru.turikhay.tlauncher.bootstrap.launcher.LocalLauncherTask;
import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.com.google.gson.Gson;
import shaded.com.google.gson.reflect.TypeToken;
import shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class BootstrapDebug {

    public static void main(String[] args) throws Exception {
        File launcherFile = new File("launcher/target/classes");
        if(!launcherFile.isDirectory()) {
            throw new FileNotFoundException("launcher: " + launcherFile.getAbsolutePath());
        }

        File libDir = new File("lib/repo");
        if(!libDir.isDirectory()) {
            throw new FileNotFoundException("libDir: " + libDir.getAbsolutePath());
        }

        Bootstrap bootstrap = new Bootstrap(launcherFile, libDir);
        bootstrap.setupUserInterface(true);

        Task<LocalLauncherTask> localLauncherTask = bootstrap.prepareLauncher(null);

        BootBridge bootBridge = BootBridge.create(
                bootstrap.getMeta().getVersion().toString(),
                args,
                options(bootstrap.getMeta().getShortBrand())
        );

        Task<Void> startLauncherTask = bootstrap.startLauncher(localLauncherTask.call().getLauncher(), bootBridge);
        if(bootstrap.getUserInterface() != null) {
            bootstrap.getUserInterface().bindToTask(startLauncherTask);
        }

        startLauncherTask.call();
        bootBridge.waitUntilClose();

        System.exit(0);
    }

    private static String options(String brand) throws IOException {
        if("true".equals(System.getProperty("tlauncher.bootstrap.debug.external"))) {
            try {
                return UpdateMeta.fetchFor(brand).call().getOptions();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return IOUtils.toString(BootstrapDebug.class.getResourceAsStream("options.json"), U.UTF8);
    }

}
