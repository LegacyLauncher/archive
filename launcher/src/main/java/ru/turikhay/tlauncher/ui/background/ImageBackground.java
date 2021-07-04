package ru.turikhay.tlauncher.ui.background;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
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

public final class ImageBackground extends JComponent implements ISwingBackground {
    private static final Logger LOGGER = LogManager.getLogger(ImageBackground.class);

    private static ImageBackground lastInstance;

    private Image defaultImage, currentImage, renderImage;
    private boolean paused;

    ImageBackground() {
        lastInstance = this;
        addComponentListener(new ExtendedComponentAdapter(this) {
            public void onComponentResized(ComponentEvent e) {
                updateRender();
            }
        });
    }

    public static ImageBackground getLastInstance() {
        return lastInstance;
    }

    public void wipe() {
        currentImage = null;
        renderImage = null;
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

            defaultImage = image;
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
            LOGGER.trace("Loading background: {} -> {}", path, input);

            try {
                image = ImageIO.read(input);
            } catch (Exception e) {
                LOGGER.error("Could not load image: {}", path, e);
                Sentry.capture(new EventBuilder()
                        .withMessage("image not found: " + path)
                        .withLevel(Event.Level.ERROR)
                        .withSentryInterface(new ExceptionInterface(e))
                );
                return;
            }

            currentImage = image;
        }

        updateRender();
    }

    private void updateRender() {
        renderImage = null;

        final Image image;
        if ((image = currentImage) == null) {
            return;
        }

        renderImage = image.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Image original, render;

        if ((original = currentImage) == null || (render = renderImage) == null) {
            return;
        }

        double
                ratio = Math.min((double) original.getWidth(null) / getWidth(), (double) original.getHeight(null) / getHeight()),
                width = original.getWidth(null) / ratio,
                height = original.getHeight(null) / ratio,
                x = (getWidth() - width) / 2.,
                y = (getHeight() - height) / 2.;

        try {
            g.drawImage(render, (int) x, (int) y, (int) width, (int) height, null);
        } catch(OutOfMemoryError oom) {
            ExceptionHandler.reduceMemory(oom);
        }
    }
}
