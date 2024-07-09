package net.legacylauncher.ui.images;

import net.legacylauncher.ui.theme.Theme;

import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

class DarkThemeAwareResourceLocator extends PrefixResourceLocator {
    DarkThemeAwareResourceLocator(ImageResourceLocator parentLoader, String nightThemeImagesPrefix) {
        super(parentLoader, Collections.singletonList(Objects.requireNonNull(nightThemeImagesPrefix)));
    }

    @Override
    public Optional<URL> loadResource(String resourceName) {
        Optional<URL> url;
        if (Theme.getTheme().useDarkTheme()) {
            url = super.loadResource(resourceName);
            if (url.isPresent()) {
                return url;
            }
        }
        return parentLoader.loadResource(resourceName);
    }
}
