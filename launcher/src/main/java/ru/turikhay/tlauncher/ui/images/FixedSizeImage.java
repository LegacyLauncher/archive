package ru.turikhay.tlauncher.ui.images;

import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedSizeImage extends JComponent {
    private final SoftReference<Image> ogImageRef;
    private final AtomicInteger tick = new AtomicInteger();
    private volatile SoftReference<BufferedImage> imageRef;

    public FixedSizeImage(Image image) {
        this.ogImageRef = new SoftReference<>(image);
        this.imageRef = new SoftReference<>(null);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                invalidateImage();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                invalidateImage();
            }
        });
    }

    private void invalidateImage() {
        final int expectedTick = tick.incrementAndGet();
        AsyncThread.execute(() -> invalidateImage(expectedTick));
    }

    private void invalidateImage(int expectedTick) {
        Image ogImage = ogImageRef.get();
        if (ogImage == null) {
            imageRef.clear();
            return;
        }
        Dimension size = SwingUtil.waitAndReturn(this::getSize);
        if (tick.get() != expectedTick) {
            return;
        }
        Image image = imageRef.get();
        if (image != null && image.getWidth(null) == size.width && image.getHeight(null) == size.height) {
            return;
        }
        double scalingFactor = SwingUtil.getScalingFactor();
        BufferedImage newImage;
        try {
            newImage = new BufferedImage((int) (scalingFactor * size.width), (int) (scalingFactor * size.height), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = (Graphics2D) newImage.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(ogImage, 0, 0, newImage.getWidth(), newImage.getHeight(), null);
        } catch (OutOfMemoryError oom) {
            ExceptionHandler.reduceMemory(oom);
            newImage = Images.ONE_PIX.get();
        }
        if (tick.get() != expectedTick) {
            return;
        }
        imageRef = new SoftReference<>(newImage);
        SwingUtil.later(this::repaint);
    }

    @Override
    public void paint(Graphics g0) {
        Image image = imageRef.get();
        if (image == null) {
            return;
        }
        Graphics2D g = (Graphics2D) g0;
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
    }
}
