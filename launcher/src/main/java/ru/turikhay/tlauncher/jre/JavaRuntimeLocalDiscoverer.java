package ru.turikhay.tlauncher.jre;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static ru.turikhay.tlauncher.jre.JavaPlatform.CURRENT_PLATFORM_CANDIDATES;

public class JavaRuntimeLocalDiscoverer {
    private final File rootDir;

    public JavaRuntimeLocalDiscoverer(File rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir);
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("rootDir doesn't exist");
        }
    }

    public File getRootDir() {
        return rootDir;
    }

    public Optional<JavaRuntimeLocal> getRuntime(String platform, String name) {
        File runtimeDir = getRuntimeDir(platform, name);
        if (!runtimeDir.isDirectory() || Optional.ofNullable(runtimeDir.listFiles()).orElse(new File[0]).length == 0) {
            return Optional.empty();
        }
        return Optional.of(new JavaRuntimeLocal(name, platform, runtimeDir));
    }

    public Optional<JavaRuntimeLocal> getCurrentPlatformRuntime(String name) {
        return CURRENT_PLATFORM_CANDIDATES
                .stream()
                .map(platform -> getRuntime(platform, name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private File getRuntimeDir(String platform, String name) {
        return new File(rootDir, name + File.separatorChar + Objects.requireNonNull(platform, "platform"));
    }
}
