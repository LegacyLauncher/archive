package ru.turikhay.tlauncher.bootstrap.util;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

public final class U {
    public static void log(String prefix, Object... o) {
        synchronized (System.out) {
            System.out.print(Instant.now().toString());
            System.out.print(' ');
            System.out.print(prefix);
            System.out.print(' ');
            if (o == null) {
                System.out.println("null");
            } else {
                for (Object anO : o) {
                    if (anO instanceof String) {
                        System.out.print(anO);
                    } else if (anO instanceof Throwable) {
                        System.out.println();
                        System.out.println(ExceptionUtils.getStackTrace((Throwable) anO));
                        //System.err.println(toString((Throwable) anO));
                    } else {
                        System.out.print(" ");
                        System.out.print(anO);
                    }
                }
                System.out.println();
            }
        }
    }

    public static Proxy getProxy() {
        return Proxy.NO_PROXY;
    }

    public static File getJar(Class<?> clazz) {
        try {
            return new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Locale getLocale(String locale) {
        if (locale == null) {
            return null;
        }
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.toString().equals(locale)) {
                return l;
            }
        }
        return null;
    }

    public static long queryFreeSpace(Path path) {
        FileSystem fileSystem = path.getFileSystem();
        if (fileSystem.isReadOnly()) {
            U.log("[NIO]", "Filesystem is read-only", path);
            return 0;
        }
        FileStore fileStore;
        try {
            fileStore = fileSystem.provider().getFileStore(path);
        } catch (IOException e) {
            U.log("[NIO]", "Couldn't get file store of", path, e);
            return -1;
        }
        if (fileStore.isReadOnly()) {
            U.log("[NIO]", "File store is read-only", fileStore);
            return 0;
        }
        try {
            return fileStore.getUsableSpace();
        } catch (IOException e) {
            U.log("[NIO]", "Can't query usable space on", fileStore, e);
            return -1;
        }
    }

    public static String getFormattedVersion(Version version) {
        StringBuilder sb = new StringBuilder();
        sb.append(version.getMajorVersion());
        sb.append(".");
        sb.append(version.getMinorVersion());
        sb.append(".");
        sb.append(version.getPatchVersion());
        if (!version.getPreReleaseVersion().isEmpty()) {
            sb.append("-").append(version.getPreReleaseVersion());
        }
        if (!version.getBuildMetadata().isEmpty()) {
            sb.append("+").append(version.getBuildMetadata());
        }
        return sb.toString();
    }

    private U() {
    }
}
