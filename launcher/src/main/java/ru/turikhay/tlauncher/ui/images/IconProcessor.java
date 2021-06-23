package ru.turikhay.tlauncher.ui.images;

import java.awt.*;
import java.awt.image.BufferedImage;

interface IconProcessor {
    Image processBaseIcon(BufferedImage baseIcon, String iconName, int targetSize);

    static Image scaleIcon(BufferedImage icon, int targetSize) {
        return icon.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
    }
}
