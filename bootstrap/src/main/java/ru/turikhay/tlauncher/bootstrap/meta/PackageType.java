package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;

public enum PackageType {
    JAR, EXE;

    public static final PackageType CURRENT;
    static {
        PackageType current = PackageType.JAR;
        File jar = U.getJar(PackageType.class);
        if(jar.exists()) {
            if(jar.isFile()) {
                if(jar.getName().endsWith(".exe")) {
                    current = PackageType.EXE;
                }
            } else {
                log("Are we packed into... directory?");
            }
        } else {
            log("Error determining class location");
        }
        CURRENT = current;
    }

    private static void log(Object...o) {
        U.log("[PackageType]", o);
    }
}
