package net.legacylauncher.ui.images;

import java.awt.*;

interface MultiResInterface {
    MultiResInterface INSTANCE = new MultiResInterfaceImpl();

    boolean isEnabled();

    Image createImage(Image... images);

    IconProcessor createIconProcessor(double scalingFactor);
}
