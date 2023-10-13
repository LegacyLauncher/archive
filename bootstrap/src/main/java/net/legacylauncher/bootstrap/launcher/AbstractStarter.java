package net.legacylauncher.bootstrap.launcher;

import net.legacylauncher.bootstrap.task.Task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStarter extends Task<Void> {
    public AbstractStarter() {
        super("startLauncher");
    }

    protected List<Path> buildClassPath(LocalLauncher launcher) throws IOException {
        List<Path> classpath = new ArrayList<>();
        classpath.add(launcher.getFile());

        Path libFolder = launcher.getLibFolder();
        for (Library lib : launcher.getMeta().getLibraries()) {
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
