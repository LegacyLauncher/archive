package net.legacylauncher.ui.images;

import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

import static net.legacylauncher.ui.images.IconProcessor.scaleIcon;

class MultiResIconProcessor implements IconProcessor {
    private final double scalingFactor;

    public MultiResIconProcessor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    @Override
    public BaseMultiResolutionImage processBaseIcon(BufferedImage baseIcon, String iconName, int targetSize) {
        return new BaseMultiResolutionImage(
                scaleIcon(baseIcon, targetSize),
                scaleIcon(baseIcon, (int) (scalingFactor * targetSize))
        );
    }
}
