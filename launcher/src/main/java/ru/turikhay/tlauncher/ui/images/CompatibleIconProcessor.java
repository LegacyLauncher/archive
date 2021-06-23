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
        if(MULTI_RES_AVAILABLE && scalingFactor != 1.0) {
            processor = new MultiResIconProcessor(scalingFactor);
        } else {
            processor = new PlainIconProcessor();
        }
        return new CompatibleIconProcessor(processor);
    }


    private static final boolean MULTI_RES_AVAILABLE;
    static {
        boolean status = true;
        try {
            Class.forName("java.awt.image.MultiResolutionImage");
        } catch (ClassNotFoundException e) {
            status = false;
        }
        MULTI_RES_AVAILABLE = status;
    }

    static boolean isMultiResAvailable() {
        return MULTI_RES_AVAILABLE;
    }
}
