package net.legacylauncher.bootstrap.launcher;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.task.Task;
import net.legacylauncher.util.shared.JavaVersion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractStarter extends Task<Void> {
    public AbstractStarter() {
        super("startLauncher");
    }

    protected List<Path> buildClassPath(LocalLauncher launcher) throws IOException {
        int javaVersion = JavaVersion.getCurrent().getMajor();

        List<Path> classpath = new ArrayList<>();
        classpath.add(launcher.getFile());

        Path libFolder = launcher.getLibFolder();
        for (Library lib : launcher.getMeta().getLibraries()) {
            if (javaVersion < lib.getJavaVersion()) {
                log.info("Skipping library {} because it requires Java {}", lib.getName(), lib.getJavaVersion());
                continue;
            }
            Path file = lib.getFile(libFolder);
            if (!Files.isRegularFile(file)) {
                throw new FileNotFoundException("classpath is not found: " + file.toAbsolutePath());
            }
            classpath.add(file);
        }

        return classpath;
    }

    protected static URL[] toURLs(List<? extends Path> paths) throws IOException {
        URL[] urls = new URL[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            urls[i] = paths.get(i).toUri().toURL();
        }
        return urls;
    }
}
