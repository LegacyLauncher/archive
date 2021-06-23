package ru.turikhay.tlauncher.ui.images;

import java.net.URL;
import java.util.Optional;

class ClassResourceLocator implements ImageResourceLocator {
    private final Class<?> baseClass;

    public ClassResourceLocator(Class<?> baseClass) {
        this.baseClass = baseClass;
    }

    @Override
    public Optional<URL> loadResource(String resourceName) {
        return Optional.ofNullable(baseClass.getResource(resourceName));
    }
}
