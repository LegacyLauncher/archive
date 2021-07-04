package ru.turikhay.tlauncher.ui.images;

import java.net.URL;
import java.util.Optional;

interface ImageResourceLocator {
    Optional<URL> loadResource(String resourceName);
}
