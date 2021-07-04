package ru.turikhay.tlauncher.bootstrap.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BaseMultiResolutionImage;
import java.net.URL;

public class SwingImageIconImpl implements SwingImageIcon {
    @Override
    public ImageIcon _loadIcon(URL url, int width, int height) {
        double scalingFactor = queryScalingFactor();
        Image image = Toolkit.getDefaultToolkit().createImage(url);
        return new ImageIcon(
                new BaseMultiResolutionImage(
                        image.getScaledInstance(
                                width,
                                height,
                                Image.SCALE_SMOOTH
                        ),
                        image.getScaledInstance(
                                (int) (scalingFactor * width),
                                (int) (scalingFactor * height),
                                Image.SCALE_SMOOTH
                        )
                )
        );
    }

    private static double queryScalingFactor() {
        GraphicsDevice graphicsDevice = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        GraphicsConfiguration graphicsConfig = graphicsDevice
                .getDefaultConfiguration();

        AffineTransform tx = graphicsConfig.getDefaultTransform();
        double scaleX = tx.getScaleX();
        double scaleY = tx.getScaleY();

        return Math.max(scaleX, scaleY);
    }
}
