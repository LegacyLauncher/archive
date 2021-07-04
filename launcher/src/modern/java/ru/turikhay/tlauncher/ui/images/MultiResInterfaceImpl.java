package ru.turikhay.tlauncher.ui.images;

import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;

class MultiResInterfaceImpl implements MultiResInterface {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public BaseMultiResolutionImage createImage(Image... images) {
        return new BaseMultiResolutionImage(images);
    }

    @Override
    public IconProcessor createIconProcessor(double scalingFactor) {
        return new MultiResIconProcessor(scalingFactor);
    }
}
