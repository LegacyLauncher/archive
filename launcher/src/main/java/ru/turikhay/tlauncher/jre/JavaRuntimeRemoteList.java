package ru.turikhay.tlauncher.jre;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static ru.turikhay.tlauncher.jre.JavaPlatform.CURRENT_PLATFORM_CANDIDATES;

public class JavaRuntimeRemoteList {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeRemoteList.class);

    public static final String URL = "https://launchermeta.mojang.com/v1/products/java-runtime/" +
            "2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";

    private final Map<String, Platform> perPlatform;

    public JavaRuntimeRemoteList(Map<String, Platform> perPlatform) {
        this.perPlatform = perPlatform;
    }

    public List<JavaRuntimeRemote> getRuntimesFor(List<String> platformCandidates) {
        return platformCandidates.stream()
                .map(perPlatform::get)
                .filter(Objects::nonNull)
                .flatMap(platform -> platform.runtimes.stream())
                .collect(Collectors.toList());
    }

    public Optional<JavaRuntimeRemote> getRuntimeFirstCandidate(List<String> platformCandidates, String name) {
        List<JavaRuntimeRemote> runtimeCandidates = getRuntimesFor(platformCandidates)
                .stream()
                .filter(runtime -> runtime.getName().equals(name))
                .collect(Collectors.toList());
        return runtimeCandidates.size() < 1 ? Optional.empty() : Optional.of(runtimeCandidates.get(0));
    }

    public Optional<JavaRuntimeRemote> getCurrentPlatformFirstRuntimeCandidate(String name) {
        return getRuntimeFirstCandidate(CURRENT_PLATFORM_CANDIDATES, name);
    }

    static class Platform {
        private final List<JavaRuntimeRemote> runtimes;

        public Platform(String name, List<JavaRuntimeRemote> runtimes) {
            this.runtimes = runtimes;
        }
    }
}
