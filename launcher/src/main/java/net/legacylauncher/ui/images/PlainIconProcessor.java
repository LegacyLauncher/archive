package net.legacylauncher.ui.images;

import java.awt.*;
import java.awt.image.BufferedImage;

import static net.legacylauncher.ui.images.IconProcessor.scaleIcon;

class PlainIconProcessor implements IconProcessor {
    @Override
    public Image processBaseIcon(BufferedImage baseIcon, String iconName, int targetSize) {
        return scaleIcon(baseIcon, targetSize);
    }
}
