package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.task.Task;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassLoaderStarter {
    public static Task<Void> start(final LocalLauncher launcher, final BootBridge bridge) {
        Objects.requireNonNull(launcher, "LocalLauncher");

        return new Task<Void>("startLauncher") {
            @Override
            protected Void execute() throws Exception {
                List<URL> jarUrlList = new ArrayList<>();
                jarUrlList.add(launcher.getFile().toUri().toURL());

                Path libFolder = launcher.getLibFolder();
                for (Library lib : launcher.getMeta().getLibraries()) {
                    Path file = lib.getFile(libFolder);
                    if (!Files.isRegularFile(file)) {
                        throw new FileNotFoundException("classpath is not found: " + file.toAbsolutePath());
                    }
                    jarUrlList.add(file.toUri().toURL());
                }

                for (URL url : jarUrlList) {
                    log("Classpath entry:", url);
                }

                URLClassLoader childCl = new URLClassLoader(jarUrlList.toArray(new URL[0]), getClass().getClassLoader());
                Class<?> clazz = Class.forName(launcher.getMeta().getMainClass(), true, childCl);

                Method method = clazz.getMethod("launch", BootBridge.class);

                try {
                    method.invoke(null, bridge);
                } catch (InvocationTargetException invokeException) {
                    if (invokeException.getCause() != null && invokeException.getCause() instanceof Exception) {
                        throw (Exception) invokeException.getCause();
                    }
                    throw invokeException;
                }

                return null;
            }

            @Override
            protected void interrupted() {
                bridge.setInterrupted();
            }
        };
    }
}
