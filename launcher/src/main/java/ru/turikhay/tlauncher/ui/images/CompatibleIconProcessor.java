package ru.turikhay.tlauncher.ui.images;

import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

class CompatibleIconProcessor implements IconProcessor {
    private final IconProcessor delegateProcessor;

    CompatibleIconProcessor(IconProcessor delegateProcessor) {
        this.delegateProcessor = delegateProcessor;
    }

    @Override
    public Image processBaseIcon(BufferedImage baseIcon, String iconName, int targetSize) {
        return delegateProcessor.processBaseIcon(baseIcon, iconName, targetSize);
    }

    static CompatibleIconProcessor create() {
        return create(SwingUtil.getScalingFactor());
    }

    private static CompatibleIconProcessor create(double scalingFactor) {
        IconProcessor processor;
        if (isMultiResAvailable() && scalingFactor != 1.0) {
            processor = MultiResInterface.INSTANCE.createIconProcessor(scalingFactor);
        } else {
            processor = new PlainIconProcessor();
        }
        return new CompatibleIconProcessor(processor);
    }

    static boolean isMultiResAvailable() {
        return MultiResInterface.INSTANCE.isEnabled();
    }
}
