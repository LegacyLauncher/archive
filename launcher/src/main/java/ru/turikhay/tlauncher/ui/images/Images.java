package ru.turikhay.tlauncher.ui.images;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.Lazy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Images {
    private static final Logger LOGGER = LogManager.getLogger(Images.class);

    private static final Pattern ICON_FILENAME_PATTERN = Pattern.compile("([A-Za-z-._]+)@([\\d]+)\\.png");

    private static final ImageResourceLocator RESOURCE_LOCATOR = new DuplicateAwareResourceLocator(
            new DarkThemeAwareResourceLocator(
                    new PrefixResourceLocator(
                            new ClassResourceLocator(Images.class),
                            Arrays.asList("fa/", "")
                    ),
                    "dark/"
            )
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
        URL url;
        try {
            url = findLocation(resourceName);
        } catch (ResourceNotFoundException e) {
            return reportMissingAndReturnEmptyImage(e);
        }
        BufferedImage baseImage;
        try {
            baseImage = loadImageByUrl(url);
        } catch (ResourceLoadException e) {
            return reportLoadErrorAndReturnEmptyImage(e);
        }
        return ICON_PROCESSOR.processBaseIcon(baseImage, name, size);
    }

    public static BufferedImage loadImageByName(String resourceName) {
        URL url;
        try {
            url = findLocation(resourceName);
        } catch (ResourceNotFoundException e) {
            return reportMissingAndReturnEmptyImage(e);
        }
        try {
            return loadImageByUrl(url);
        } catch (ResourceLoadException e) {
            return reportLoadErrorAndReturnEmptyImage(e);
        }
    }

    public static Image loadIconById(String id) {
        Matcher isIcon = ICON_FILENAME_PATTERN.matcher(
                Objects.requireNonNull(id, "id")
        );
        if (!isIcon.matches()) {
            throw new IllegalArgumentException("bad icon id: " + id);
        }
        String name = isIcon.group(1);
        int size = Integer.parseInt(isIcon.group(2));
        return loadIcon(name, size);
    }

    private static URL findLocation(String resourceName) throws ResourceNotFoundException {
        Optional<URL> url = RESOURCE_LOCATOR.loadResource(resourceName);
        if (!url.isPresent()) {
            throw new ResourceNotFoundException(resourceName);
        }
        return url.get();
    }

    private static BufferedImage loadImageByUrl(URL url) throws ResourceLoadException {
        Objects.requireNonNull(url, "url");
        BufferedImage image;
        try {
            image = ImageIO.read(url);
        } catch (IOException e) {
            throw new ResourceLoadException(url, e);
        }
        if (image == null) {
            throw new ResourceLoadException(url, new NullPointerException("ImageIO.read"));
        }
        return image;
    }

    public static final Lazy<BufferedImage> ONE_PIX = Lazy.of(() ->
            new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR)
    );

    private static BufferedImage reportMissingAndReturnEmptyImage(ResourceNotFoundException e) {
        LOGGER.error("Missing resource", e);
        Sentry.capture(new EventBuilder()
                .withLevel(Event.Level.ERROR)
                .withMessage("missing resource: " + e.getMessage())
                .withSentryInterface(new ExceptionInterface(e))
        );
        return ONE_PIX.get();
    }

    private static BufferedImage reportLoadErrorAndReturnEmptyImage(ResourceLoadException e) {
        LOGGER.error("Resource cannot be loaded", e);
        Sentry.capture(new EventBuilder()
                .withLevel(Event.Level.ERROR)
                .withMessage("resource loading error")
                .withSentryInterface(new ExceptionInterface(e))
                .withExtra("resourceUrl", String.valueOf(e.getUrl()))
        );
        return ONE_PIX.get();
    }

    private static String toIconResourceName(String name) {
        return String.format(Locale.ROOT, "%s.png", name);
    }

    public static ImageIcon getIcon16(String name) {
        return getIcon(name, 16);
    }

    public static Lazy<ImageIcon> getIcon16Lazy(String name) {
        return Lazy.of(() -> getIcon16(name));
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
}
