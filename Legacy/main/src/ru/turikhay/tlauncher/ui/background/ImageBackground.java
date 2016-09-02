package ru.turikhay.tlauncher.ui.background;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.util.U;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

public final class ImageBackground extends JComponent implements ISwingBackground {
    private SoftReference<Image> defaultImage, currentImage, renderImage;
    private boolean paused;

    ImageBackground() {
        addComponentListener(new ExtendedComponentAdapter(this) {
            public void onComponentResized(ComponentEvent e) {
                updateRender();
            }
        });
    }

    @Override
    public void onResize() {
        if (getParent() != null) {
            setSize(getParent().getSize());
        }
    }

    @Override
    public void startBackground() {
        paused = false;
        updateRender();
    }

    @Override
    public void pauseBackground() {
        paused = true;
    }

    @Override
    public void loadBackground(String path) throws Exception {
        if (defaultImage == null) {
            Image image;

            try {
                image = Images.getImage("plains.jpg");
            } catch (Exception e) {
                throw new Error("could not load default image", e);
            }

            defaultImage = new SoftReference<Image>(image);
        }

        renderImage = null;

        if (path == null) {
            currentImage = defaultImage;
        } else {
            currentImage = null;

            InputStream input = null;

            if (U.makeURL(path) == null) {
                File file = new File(path);
                if (file.isFile()) {
                    input = new FileInputStream(file);
                } else {
                    throw new FileNotFoundException(path);
                }
            } else {
                input = U.makeURL(path).openStream();
            }

            if (input == null) {
                throw new IllegalArgumentException("could not parse path: " + path);
            }

            Image image;
            log("Loading background", path, input);

            try {
                image = ImageIO.read(input);
            } catch (Exception e) {
                log("Could not load image", path, e);
                return;
            }

            currentImage = new SoftReference<Image>(image);
        }

        updateRender();
    }

    private void updateRender() {
        renderImage = null;

        final Image image;
        if (currentImage == null || (image = currentImage.get()) == null) {
            return;
        }

        renderImage = new SoftReference<Image>(image.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH));
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        final Image original, render;

        if (currentImage == null || (original = currentImage.get()) == null || renderImage == null || (render = renderImage.get()) == null) {
            return;
        }

        double
                ratio = Math.min((double) original.getWidth(null) / getWidth(), (double) original.getHeight(null) / getHeight()),
                width = original.getWidth(null) / ratio,
                height = original.getHeight(null) / ratio,
                x = (getWidth() - width) / 2.,
                y = (getHeight() - height) / 2.;

        g.drawImage(render, (int) x, (int) y, (int) width, (int) height, null);
    }

    private void log(Object... o) {
        U.log("[Background][Image]", o);
    }
}
