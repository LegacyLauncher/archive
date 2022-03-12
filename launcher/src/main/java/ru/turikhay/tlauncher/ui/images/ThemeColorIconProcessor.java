package ru.turikhay.tlauncher.ui.images;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.theme.Theme;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Objects;
import java.util.function.Supplier;

public class ThemeColorIconProcessor implements IconProcessor {
    private static final Logger LOGGER = LogManager.getLogger(ThemeColorIconProcessor.class);

    private final IconProcessor parentProcessor;
    private final Supplier<Theme> themeSupplier;
    private final Color defaultColor;

    public ThemeColorIconProcessor(IconProcessor parentProcessor,
                                   Supplier<Theme> themeSupplier,
                                   Color defaultColor) {
        this.parentProcessor = Objects.requireNonNull(parentProcessor, "parentProcessor");
        this.themeSupplier = Objects.requireNonNull(themeSupplier, "themeSupplier");
        this.defaultColor = Objects.requireNonNull(defaultColor, "defaultColor");
    }

    public ThemeColorIconProcessor(Supplier<Theme> themeSupplier, IconProcessor parentProcessor) {
        this(parentProcessor, themeSupplier, Color.BLACK);
    }

    public ThemeColorIconProcessor(IconProcessor parentProcessor) {
        this(Theme::getTheme, parentProcessor);
    }

    @Override
    public Image processBaseIcon(BufferedImage baseIcon, String iconName, int targetSize) {
        BufferedImage icon;
        if (canPaintColor(baseIcon, iconName)) {
            int targetColor = requestIconColor(iconName);
            icon = paintIcon(baseIcon, targetColor);
        } else {
            icon = baseIcon;
        }
        return parentProcessor.processBaseIcon(icon, iconName, targetSize);
    }

    private BufferedImage paintIcon(BufferedImage baseIcon, int targetColor) {
        int color = 0x00ffffff & targetColor;
        BufferedImage colorIcon = new BufferedImage(
                baseIcon.getWidth(),
                baseIcon.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        for (int x = 0; x < baseIcon.getWidth(); x++) {
            for (int y = 0; y < baseIcon.getHeight(); y++) {
                int rgb = baseIcon.getRGB(x, y);
                rgb |= color;
                colorIcon.setRGB(x, y, rgb);
            }
        }
        return colorIcon;
    }

    private int requestIconColor(String iconName) {
        Theme theme = Objects.requireNonNull(themeSupplier.get(), "theme");
        Color iconColor = theme.getIconColor(iconName);
        if (iconColor == null) {
            iconColor = defaultColor;
        }
        return iconColor.getRGB();
    }

    private static boolean canPaintColor(BufferedImage icon, String iconName) {
        ColorModel colorModel = icon.getColorModel();
        if (colorModel == null) {
            LOGGER.warn("Unknown color model of icon {}", iconName);
            return false;
        }
        int type = colorModel.getColorSpace().getType();
        return type == ColorSpace.TYPE_GRAY;
    }
}
