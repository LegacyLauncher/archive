package ru.turikhay.tlauncher.ui.background;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.tlauncher.ui.FlatLaf;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

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
            String defaultImageName = "plains.jpg";
            if (TLauncher.getInstance() != null) {
                Optional<FlatLafConfiguration> flatLafConfiguration = TLauncher.getInstance().getSettings()
                        .getFlatLafConfiguration();
                Optional<FlatLafConfiguration.Theme> selectedTheme = FlatLaf.getSelectedNowTheme(flatLafConfiguration);
                if(flatLafConfiguration.isPresent() && selectedTheme.isPresent()) {
                    defaultImageName = "plains4K.jpg";
                    String selectedThemeFile = flatLafConfiguration.get().getThemeFiles().get(selectedTheme.get());
                    if (selectedThemeFile != null) {
                        BufferedImage externalBackgroundImage = FlatLaf
                                .loadDefaultBackgroundFromThemeFile(selectedThemeFile);
                        if (externalBackgroundImage != null) {
                            defaultImage = externalBackgroundImage;
                        }
                    }
                }
            }
            if (defaultImage == null) {
                defaultImage = Images.loadImageByName(defaultImageName);
            }
        }

        renderImage = null;

        if (path == null) {
            currentImage = defaultImage;
        } else {
            currentImage = null;

            InputStream input;

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

        renderImage = renderImage(image);
        repaint();
    }

    private Image renderImage(Image image) {
        /*if(MultiResInterface.INSTANCE.isEnabled()) {
            return renderMultiResImage(image);
        } else {*/
        return renderScaleImage(image);
        //}
    }

    private Image renderScaleImage(Image image) {
        double realWidth = getWidth() * SwingUtil.getScalingFactor(),
                realHeight = getHeight() * SwingUtil.getScalingFactor();
        BufferedImage scaledImage;
        Graphics2D g;
        try {
            scaledImage = new BufferedImage((int) realWidth, (int) realHeight, BufferedImage.TYPE_3BYTE_BGR);
            g = (Graphics2D) scaledImage.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(image, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
        } catch (OutOfMemoryError oom) {
            ExceptionHandler.reduceMemory(oom);
            return Images.ONE_PIX.get();
        }
        return scaledImage;
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
        } catch (OutOfMemoryError oom) {
            ExceptionHandler.reduceMemory(oom);
        }
    }
}
