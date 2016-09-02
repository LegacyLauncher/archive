package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

class ClassLoaderStarter implements IStarter {
    @Override
    public Task<Void> start(final LocalLauncher launcher) {
        U.requireNotNull(launcher, "LocalLauncher");

        return new Task<Void>("ClassLoaderStarter{"+ launcher +"}") {
            @Override
            protected Void execute() throws Exception {
                URLClassLoader childCl = new URLClassLoader(new URL[]{ launcher.getFile().toURI().toURL() }, getClass().getClassLoader());
                Class<?> clazz = Class.forName (launcher.getMeta().getMainClass(), true, childCl);
                Method method = clazz.getMethod("main", String[].class);
                method.invoke(null, (Object) new String[0]);
                return null;
            }
        };
    }
}
