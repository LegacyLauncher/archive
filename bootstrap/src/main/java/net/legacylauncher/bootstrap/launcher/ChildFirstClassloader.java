package net.legacylauncher.bootstrap.launcher;

import java.net.URL;
import java.net.URLClassLoader;

public class ChildFirstClassloader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final ClassLoader system;

    public ChildFirstClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        system = getSystemClassLoader();
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                try {
                    loadedClass = findClass(name);
                } catch (ClassNotFoundException e) {
                    try {
                        loadedClass = super.loadClass(name, resolve);
                    } catch (ClassNotFoundException e1) {
                        try {
                            loadedClass = system.loadClass(name);
                        } catch (ClassNotFoundException e2) {
                            e.addSuppressed(e1);
                            e.addSuppressed(e2);
                            throw e;
                        }
                    }
                }
            }

            if (resolve) {
                resolveClass(loadedClass);
            }

            return loadedClass;
        }
    }
}
