package net.legacylauncher.bootstrap.launcher;

import net.legacylauncher.bootstrap.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class SharedClassesStarter extends AbstractStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SharedClassesStarter.class);
    private final LocalLauncher launcher;
    private final BootBridge bridge;

    private SharedClassesStarter(final LocalLauncher launcher, final BootBridge bridge) {
        this.launcher = launcher;
        this.bridge = bridge;
    }

    @Override
    protected Void execute() throws Exception {
        List<Path> classpath = buildClassPath(launcher);

        for (Path path : classpath) {
            LOGGER.info("Classpath entry: {}", path);
        }

        URLClassLoader childCl = new URLClassLoader(toURLs(classpath), Thread.currentThread().getContextClassLoader());
        Class<?> clazz = Class.forName(launcher.getMeta().getBridgedEntryPoint(), true, childCl);

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

    public static Task<Void> start(final LocalLauncher launcher, final BootBridge bridge) {
        Objects.requireNonNull(launcher, "LocalLauncher");
        Objects.requireNonNull(bridge, "BootBridge");

        return new SharedClassesStarter(launcher, bridge);
    }
}
