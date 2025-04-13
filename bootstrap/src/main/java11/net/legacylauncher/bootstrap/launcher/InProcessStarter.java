package net.legacylauncher.bootstrap.launcher;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.ipc.DBusBootstrapIPC;
import net.legacylauncher.bootstrap.ipc.DBusResolverIPC;
import net.legacylauncher.bootstrap.meta.LocalLauncherMeta;
import net.legacylauncher.bootstrap.task.Task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

@Slf4j
public class InProcessStarter extends AbstractDBusStarter {
    private InProcessStarter(LocalLauncher launcher, DBusBootstrapIPC ipc, DBusResolverIPC resolverIpc) {
        super(launcher, ipc, resolverIpc);
    }

    public static Task<Void> start(final LocalLauncher launcher, final DBusBootstrapIPC ipc, final DBusResolverIPC resolverIpc) {
        Objects.requireNonNull(launcher, "LocalLauncher");
        Objects.requireNonNull(ipc, "DBusBootstrapIPC");

        return new InProcessStarter(launcher, ipc, resolverIpc);
    }

    @Override
    protected Void execute() throws Exception {
        Future<Void> dbusServerReady = prepareDBusServer();
        List<Path> classpath = buildClassPath(launcher);

        for (Path path : classpath) {
            log.trace("Classpath entry: {}", path);
        }

        URLClassLoader classLoader = new NamedChildFirstClassloader("launcher", toURLs(classpath), ClassLoader.getPlatformClassLoader());
        LocalLauncherMeta.Entrypoint entrypoint = launcher.getMeta().getEntrypoint(LocalLauncherMeta.EntrypointType.DBusP2P);
        Class<?> clazz = classLoader.loadClass(entrypoint.getType());
        Method method = clazz.getMethod(entrypoint.getMethod(), String.class);

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
}
