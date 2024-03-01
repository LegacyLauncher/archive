package net.legacylauncher.bootstrap.launcher;

import net.legacylauncher.bootstrap.ipc.DBusBootstrapIPC;
import net.legacylauncher.bootstrap.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

public class InProcessStarter extends AbstractDBusStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(InProcessStarter.class);
    private InProcessStarter(LocalLauncher launcher, DBusBootstrapIPC ipc) {
        super(launcher, ipc);
    }

    @Override
    protected Void execute() throws Exception {
        Future<Void> dbusServerReady = prepareDBusServer();
        List<Path> classpath = buildClassPath(launcher);

        for (Path path : classpath) {
            LOGGER.info("Classpath entry: {}", path);
        }

        URLClassLoader classLoader = new URLClassLoader("launcher", toURLs(classpath), ClassLoader.getPlatformClassLoader());
        Class<?> clazz = classLoader.loadClass(Objects.requireNonNull(launcher.getMeta().getEntryPoint(), "LocalLauncher entryPoint"));
        Method method = clazz.getMethod("launchP2P", String.class);

        dbusServerReady.get();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            method.invoke(null, getBusAddress().toString());
        } catch (InvocationTargetException invokeException) {
            if (invokeException.getCause() != null && invokeException.getCause() instanceof Exception) {
                throw (Exception) invokeException.getCause();
            }
            throw invokeException;
        }

        return null;
    }


    public static Task<Void> start(final LocalLauncher launcher, final DBusBootstrapIPC ipc) {
        Objects.requireNonNull(launcher, "LocalLauncher");
        Objects.requireNonNull(ipc, "DBusBootstrapIPC");

        return new InProcessStarter(launcher, ipc);
    }

}
