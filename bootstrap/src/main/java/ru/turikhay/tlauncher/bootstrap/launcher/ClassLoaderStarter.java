package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.Bootstrap;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.DataBuilder;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ClassLoaderStarter {
    public static Task<Void> start(final LocalLauncher launcher, final BootBridge bridge) {
        U.requireNotNull(launcher, "LocalLauncher");

        return new Task<Void>("startLauncher") {
            @Override
            protected Void execute() throws Exception {
                List<URL> jarUrlList = new ArrayList<URL>();
                jarUrlList.add(launcher.getFile().toURI().toURL());

                File libFolder = launcher.getLibFolder();
                for(Library lib : launcher.getMeta().getLibraries()) {
                    File file = lib.getFile(libFolder);
                    if(!file.isFile()) {
                        throw new FileNotFoundException("classpath is not found: " + file.getAbsolutePath());
                    }
                    jarUrlList.add(file.toURI().toURL());
                }

                for(URL url : U.toArray(jarUrlList, URL.class)) {
                    log("Classpath entry:", url);
                }

                URLClassLoader childCl = new URLClassLoader(U.toArray(jarUrlList, URL.class), getClass().getClassLoader());
                Class<?> clazz = Class.forName(launcher.getMeta().getMainClass(), true, childCl);

                Method method = clazz.getMethod("launch", BootBridge.class);

                try {
                    method.invoke(null, bridge);
                } catch(InvocationTargetException invokeException) {
                    if(invokeException.getCause() != null && invokeException.getCause() instanceof Exception) {
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
