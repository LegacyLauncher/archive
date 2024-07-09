package net.legacylauncher.ui.background;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.handlers.ExceptionHandler;
import net.legacylauncher.logger.LoggerBuffer;
import net.legacylauncher.ui.FlatLaf;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.swing.extended.ExtendedComponentAdapter;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.shared.FlatLafConfiguration;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
public final class ImageBackground extends JComponent implements ISwingBackground {
    private static final long FILE_MAX_LENGTH = 1024*1024*4;

    private static ImageBackground lastInstance;

    private Image defaultImage, currentImage, renderImage;
    private final boolean ready;

    ImageBackground() {
        lastInstance = this;
        addComponentListener(new ExtendedComponentAdapter(this) {
            @Override
            public void onComponentResized() {
                updateRender();
            }
        });
        ready = true;
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
        updateRender();
    }

    @Override
    public void pauseBackground() {
    }

    @Override
    public void loadBackground(String path) throws Exception {
        if (defaultImage == null) {
            updateDefaultImage();
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
                    input = Files.newInputStream(file.toPath());
                } else {
                    throw new FileNotFoundException(path);
                }
                if (file.length() > FILE_MAX_LENGTH) {
                    log.warn("Background image is too big to load: {}", file.getAbsolutePath());
                    return;
                }
            } else {
                input = U.makeURL(path).openStream();
            }

            if (input == null) {
                throw new IllegalArgumentException("could not parse path: " + path);
            }

            Image image;
            log.trace("Loading background: {} -> {}", path, input);

            try {
                image = ImageIO.read(input);
            } catch (Exception e) {
                log.error("Could not load image: {}", path, e);
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

    @Override
    public void updateUI() {
        if (ready) {
            boolean usesDefaultImage = currentImage == defaultImage;
            defaultImage = null;
            updateDefaultImage();
            if (usesDefaultImage) {
                currentImage = defaultImage;
                updateRender();
            }
        }
        super.updateUI();
    }

    private void updateDefaultImage() {
        String defaultImageName = "plains.jpg";
        if (LegacyLauncher.getInstance() != null) {
            Optional<FlatLafConfiguration> flatLafConfiguration = LegacyLauncher.getInstance().getSettings()
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
}
