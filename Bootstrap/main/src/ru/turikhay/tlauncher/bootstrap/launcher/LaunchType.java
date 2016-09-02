package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.util.U;

public enum LaunchType {
    AUTO,

    CLASSLOADER(new ClassLoaderStarter()),
    PROCESS(new ProcessStarter());

    private final IStarter starter;

    LaunchType(IStarter starter) {
        this.starter = U.requireNotNull(starter, "starter");
    }

    LaunchType() {
        this.starter = null;
    }

    public final IStarter getStarter() {
        if(this == AUTO) {
            return CLASSLOADER.getStarter();
        }
        return starter;
    }
}
