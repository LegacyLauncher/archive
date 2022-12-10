package ru.turikhay.tlauncher.bootstrap;

import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.launcher.LocalLauncherTask;
import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import ru.turikhay.tlauncher.bootstrap.task.Task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BootstrapDebug {

    public static void main(String[] args) throws Exception {
        Path bootstrapJar;
        try {
            bootstrapJar = Paths.get(BootstrapDebug.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to determine bootstrap jar location", e);
        }

        Path launcherFile = Paths.get("../launcher/build/tlRun").toAbsolutePath();
        if (!Files.isDirectory(launcherFile)) {
            throw new FileNotFoundException("launcher: " + launcherFile);
        }

        Path libDir = Paths.get("../lib/" + BuildConfig.SHORT_BRAND).toAbsolutePath();
        if (!Files.isDirectory(libDir)) {
            throw new FileNotFoundException("libDir: " + libDir);
        }

        Bootstrap bootstrap = new Bootstrap(
                args,
                bootstrapJar,
                TargetConfig.readConfigFromFile(TargetConfig.getDefaultConfigFilePath(BuildConfig.SHORT_BRAND)),
                launcherFile,
                libDir
        );
        bootstrap.setupUserInterface(true);

        Task<LocalLauncherTask> localLauncherTask = bootstrap.prepareLauncher(null);
        bootstrap.getBootBridge().setOptions(options(BuildConfig.SHORT_BRAND));

        Task<Void> startLauncherTask = bootstrap.startLauncher(localLauncherTask.call().getLauncher());
        if (bootstrap.getUserInterface() != null) {
            bootstrap.getUserInterface().bindToTask(startLauncherTask);
        }

        startLauncherTask.call();
        bootstrap.getBootBridge().waitUntilClose();

        System.exit(0);
    }

    private static String options(String brand) throws IOException {
        if ("true".equals(System.getProperty("tlauncher.bootstrap.debug.external"))) {
            try {
                return UpdateMeta.fetchFor(
                        brand,
                        // interrupt immediately
                        UpdateMeta.ConnectionInterrupter.Callback::onConnectionInterrupted
                ).call().getOptions();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return IOUtils.toString(BootstrapDebug.class.getResourceAsStream("options.json"), StandardCharsets.UTF_8);
    }

}
