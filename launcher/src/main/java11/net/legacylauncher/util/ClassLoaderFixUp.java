package net.legacylauncher.util;

public class ClassLoaderFixUp {
    public static void fixup() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if ("launcher".equals(loader.getName())) {
            SwingUtil.wait(() -> Thread.currentThread().setContextClassLoader(loader));
        }
    }
}
