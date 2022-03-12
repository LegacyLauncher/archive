package ru.turikhay.tlauncher.ui.images;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.SoftReference;

public class FixedSizeImage extends JComponent {
    private final SoftReference<Image> imageRef;

    public FixedSizeImage(Image image) {
        this.imageRef = new SoftReference<>(image);
    }

    @Override
    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;

        Image image = imageRef.get();
        if (image == null) {
            return;
        }

        int compWidth = getWidth(), compHeight = getHeight();
        if (compWidth <= 0 || compHeight <= 0) {
            return;
        }

        double
                imgWidth = image.getWidth(this),
                imgHeight = image.getHeight(this),
                ratio = Math.min(imgWidth / compWidth, imgHeight / compHeight),
                width = imgWidth / ratio,
                height = imgHeight / ratio,
                x = (compWidth - width) / 2.,
                y = (compHeight - height) / 2.;

        Object oldAntialiasingHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(image, (int) x, (int) y, (int) width, (int) height, this);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasingHint);
    }
}
