package net.legacylauncher.bootstrap.launcher;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.meta.LocalLauncherMeta;
import net.legacylauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SharedClassesStarter extends AbstractStarter {
    private final LocalLauncher launcher;
    private final BootBridge bridge;

    private SharedClassesStarter(final LocalLauncher launcher, final BootBridge bridge) {
        this.launcher = launcher;
        this.bridge = bridge;
    }

    public static Task<Void> start(final LocalLauncher launcher, final BootBridge bridge) {
        Objects.requireNonNull(launcher, "LocalLauncher");
        Objects.requireNonNull(bridge, "BootBridge");

        return new SharedClassesStarter(launcher, bridge);
    }

    @Override
    protected Void execute() throws Exception {
        List<Path> classpath = buildClassPath(launcher);

        for (Path path : classpath) {
            log.info("Classpath entry: {}", path);
        }

        URLClassLoader childCl = new ChildFirstClassloader(toURLs(classpath), Thread.currentThread().getContextClassLoader());
        LocalLauncherMeta.Entrypoint entrypoint = launcher.getMeta().getEntrypoint(LocalLauncherMeta.EntrypointType.Bridge);
        Class<?> clazz = Class.forName(entrypoint.getType(), true, childCl);

        Method method = clazz.getMethod(entrypoint.getMethod(), BootBridge.class);

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
}
