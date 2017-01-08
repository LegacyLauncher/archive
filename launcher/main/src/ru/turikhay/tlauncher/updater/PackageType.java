package ru.turikhay.tlauncher.updater;

import ru.turikhay.util.FileUtil;

public enum PackageType {
    EXE,
    JAR;

    public static final PackageType CURRENT;

    static {
        CURRENT = FileUtil.getRunningJar().toString().endsWith(".exe") ? EXE : JAR;
    }

    public String toLowerCase() {
        return name().toLowerCase();
    }

    public static boolean isCurrent(PackageType pt) {
        return pt == CURRENT;
    }
}
