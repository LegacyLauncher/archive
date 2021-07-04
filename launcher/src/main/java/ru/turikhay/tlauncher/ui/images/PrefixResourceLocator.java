package ru.turikhay.tlauncher.ui.images;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class PrefixResourceLocator implements ImageResourceLocator {
    protected final ImageResourceLocator parentLoader;
    private final List<String> prefixes;

    public PrefixResourceLocator(ImageResourceLocator parentLoader, List<String> prefixes) {
        this.parentLoader = Objects.requireNonNull(parentLoader, "parentLoader");
        this.prefixes = Objects.requireNonNull(prefixes, "prefixes");
    }

    @Override
    public Optional<URL> loadResource(String resourceName) {
        return prefixes.stream()
                .map(prefix -> parentLoader.loadResource(toResourceName(prefix, resourceName)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private String toResourceName(String prefix, String resourceName) {
        return prefix + resourceName;
    }
}
