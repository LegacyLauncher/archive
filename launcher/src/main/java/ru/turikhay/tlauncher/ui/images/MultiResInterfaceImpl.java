package ru.turikhay.tlauncher.ui.images;

import java.awt.*;

class MultiResInterfaceImpl implements MultiResInterface {
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Image createImage(Image... images) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IconProcessor createIconProcessor(double scalingFactor) {
        throw new UnsupportedOperationException();
    }
}
