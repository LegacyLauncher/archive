package ru.turikhay.tlauncher.ui.images;

import java.awt.*;

public interface MultiResInterface {
    MultiResInterface INSTANCE = new MultiResInterfaceImpl();

    boolean isEnabled();

    Image createImage(Image... images);

    IconProcessor createIconProcessor(double scalingFactor);
}
