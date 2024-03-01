package net.legacylauncher.bootstrap;

import net.legacylauncher.bootstrap.launcher.LocalLauncher;
import net.legacylauncher.bootstrap.launcher.LocalLauncherTask;
import net.legacylauncher.bootstrap.meta.UpdateMeta;
import net.legacylauncher.bootstrap.task.Task;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BootstrapDebug {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapDebug.class);

    public static void main(String[] args) throws Exception {
        Path bootstrapJar;
        try {
            bootstrapJar = Paths.get(BootstrapDebug.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to determine bootstrap jar location", e);
        }

        Path launcherFile = Paths.get(System.getenv("LL_LAUNCHER_JAR")).toAbsolutePath();
        if (!Files.isRegularFile(launcherFile)) {
            throw new FileNotFoundException("launcher: " + launcherFile);
        }

        Path libDir = Paths.get(System.getenv("LL_LIBRARIES_DIR")).toAbsolutePath();
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
        bootstrap.setFork("true".equalsIgnoreCase(System.getenv("LL_FORK")));
        bootstrap.setupUserInterface(true);

        Task<LocalLauncherTask> localLauncherTask = bootstrap.prepareLauncher(null);
        LocalLauncher localLauncher = localLauncherTask.call().getLauncher();
        bootstrap.initIPC(localLauncher);
        bootstrap.getBootstrapIPC().setLauncherConfiguration(options(BuildConfig.SHORT_BRAND));

        Task<Void> startLauncherTask = bootstrap.startLauncher(localLauncher);
        if (bootstrap.getUserInterface() != null) {
            bootstrap.getUserInterface().bindToTask(startLauncherTask);
        }

        startLauncherTask.call();
        bootstrap.getBootstrapIPC().waitUntilClose();

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
                LOGGER.error("Unable to fetch update meta", e);
                return null;
            }
        }
        return IOUtils.toString(BootstrapDebug.class.getResourceAsStream("options.json"), StandardCharsets.UTF_8);
    }

}
