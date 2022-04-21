package ru.turikhay.tlauncher.jre;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static ru.turikhay.tlauncher.jre.JavaPlatform.CURRENT_PLATFORM;

public class JavaRuntimeRemoteList {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeRemoteList.class);

    public static final String URL = "https://launchermeta.mojang.com/v1/products/java-runtime/" +
            "2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";

    private final Map<String, Platform> perPlatform;

    public JavaRuntimeRemoteList(Map<String, Platform> perPlatform) {
        this.perPlatform = perPlatform;
    }

    public List<JavaRuntimeRemote> getRuntimesFor(String platform) {
        Platform pl = perPlatform.get(Objects.requireNonNull(platform, "platform"));
        if (pl == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(pl.runtimes);
    }

    public Optional<JavaRuntimeRemote> getLatestRuntime(String platform, String name) {
        return getRuntimesFor(platform).stream()
                .filter(r -> name.equals(r.getName()))
                .max(Comparator.comparing(o -> o.getVersion().getReleased()));
    }

    public List<JavaRuntimeRemote> getCurrentPlatformRuntimes(String name) {
        return getRuntimesFor(CURRENT_PLATFORM);
    }

    public Optional<JavaRuntimeRemote> getCurrentPlatformLatestRuntime(String name) {
        return getLatestRuntime(CURRENT_PLATFORM, name);
    }

    static class Platform {
        private final List<JavaRuntimeRemote> runtimes;

        public Platform(String name, List<JavaRuntimeRemote> runtimes) {
            this.runtimes = runtimes;
        }
    }
}
