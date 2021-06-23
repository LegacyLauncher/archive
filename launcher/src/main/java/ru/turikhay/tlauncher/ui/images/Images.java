package ru.turikhay.tlauncher.ui.images;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Images {
    private static final Pattern ICON_FILENAME_PATTERN = Pattern.compile("([A-Za-z-._]+)@([\\d]+)\\.png");

    private static final ImageResourceLocator RESOURCE_LOCATOR =
            new DarkThemeAwareResourceLocator(
                    new PrefixResourceLocator(
                            new ClassResourceLocator(Images.class),
                            Arrays.asList("fa/", "")
                    ),
                    "dark/"
            );

    private static final IconProcessor ICON_PROCESSOR = new ThemeColorIconProcessor(CompatibleIconProcessor.create());

    static {
        // Disable disk cache
        ImageIO.setUseCache(false);
    }

    public static ImageIcon getIcon(String name, int size) {
        return new ImageIcon(loadIcon(name, size), size);
    }

    public static Image loadIcon(String name, int size) {
        String resourceName = toIconResourceName(name);
        URL url = findLocation(resourceName);
        BufferedImage baseImage = loadImageByUrl(url);
        return ICON_PROCESSOR.processBaseIcon(baseImage, name, size);
    }

    public static BufferedImage loadImageByName(String resourceName) {
        URL url = findLocation(resourceName);
        return loadImageByUrl(url);
    }

    public static Image loadIconById(String id) {
        Matcher isIcon = ICON_FILENAME_PATTERN.matcher(
                Objects.requireNonNull(id, "id")
        );
        if(!isIcon.matches()) {
            throw new IllegalArgumentException("bad icon id: " + id);
        }
        String name = isIcon.group(1);
        int size = Integer.parseInt(isIcon.group(2));
        return loadIcon(name, size);
    }

    private static URL findLocation(String resourceName) {
        Optional<URL> url = RESOURCE_LOCATOR.loadResource(resourceName);
        if(!url.isPresent()) {
            throw new Error("resource not found: " + resourceName);
        }
        return url.get();
    }

    private static BufferedImage loadImageByUrl(URL url) {
        Objects.requireNonNull(url, "url");
        try {
            return ImageIO.read(url);
        } catch (Exception e) {
            throw new Error("could not load the image", e);
        }
    }

    private static String toIconResourceName(String name) {
        return String.format(Locale.ROOT, "%s.png", name);
    }

    public static ImageIcon getIcon16(String name) {
        return getIcon(name, 16);
    }

    public static ImageIcon getIcon24(String name) {
        return getIcon(name, 24);
    }

    public static ImageIcon getIcon32(String name) {
        return getIcon(name, 32);
    }

    public static ImageIcon getIcon48(String name) {
        return getIcon(name, 48);
    }

    public static ImageIcon getIcon64(String name) {
        return getIcon(name, 64);
    }

    public static Image loadIcon16(String name) {
        return loadIcon(name, 16);
    }

    public static Image loadIcon24(String name) {
        return loadIcon(name, 24);
    }

    public static Image loadIcon32(String name) {
        return loadIcon(name, 32);
    }

    public static Image loadIcon48(String name) {
        return loadIcon(name, 48);
    }

    public static Image loadIcon64(String name) {
        return loadIcon(name, 64);
    }

    public static boolean isMultiResAvailable() {
        return CompatibleIconProcessor.isMultiResAvailable();
    }
}
