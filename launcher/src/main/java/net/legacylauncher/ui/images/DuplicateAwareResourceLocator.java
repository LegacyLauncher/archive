package net.legacylauncher.ui.images;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DuplicateAwareResourceLocator implements ImageResourceLocator {
    private static final Pattern PATTERN = Pattern.compile("(.+)-\\d+(\\..+)");

    private final ImageResourceLocator parentLoader;

    DuplicateAwareResourceLocator(ImageResourceLocator parentLoader) {
        this.parentLoader = Objects.requireNonNull(parentLoader);
    }

    @Override
    public Optional<URL> loadResource(String resourceName) {
        Matcher matcher = PATTERN.matcher(resourceName);
        String normalResourceName;
        if (matcher.matches()) {
            normalResourceName = matcher.group(1) + matcher.group(2);
        } else {
            normalResourceName = resourceName;
        }
        return parentLoader.loadResource(normalResourceName);
    }
}
