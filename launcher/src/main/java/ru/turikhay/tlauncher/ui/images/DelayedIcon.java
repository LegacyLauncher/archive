package ru.turikhay.tlauncher.ui.images;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.notice.NoticeImage;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DelayedIcon extends ExtendedLabel implements ExtendedIcon {
    private static final Logger LOGGER = LogManager.getLogger(DelayedIcon.class);

    private Image icon;
    private Dimension size;

    private volatile IconLoader loader;

    public DelayedIcon(NoticeImage image, int targetWidth, int targetHeight) {
        this();
        setImage(image, targetWidth, targetHeight, false);
    }

    public DelayedIcon() {
        setIcon(this);
        setDisabledIcon(getDisabledInstance());
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (icon != null) {
            g.drawImage(icon, x, y, null);
        }
    }

    public void setImage(Future<Image> icon,
                         int iconWidth, int iconHeight,
                         int targetWidth, int targetHeight,
                         boolean repaint) {
        this.icon = null;
        this.size = computeSize(iconWidth, iconHeight, targetWidth, targetHeight);
        AsyncThread.future(loader = new IconLoader(icon, size));
        if (repaint) {
            repaint();
        }
    }

    public void setImage(NoticeImage image, int targetWidth, int targetHeight, boolean repaint) {
        setImage(image.getTask(), image.getWidth(), image.getHeight(), targetWidth, targetHeight, repaint);
    }

    public void setImage(NoticeImage image, int targetWidth, int targetHeight) {
        setImage(image.getTask(), image.getWidth(), image.getHeight(), targetWidth, targetHeight, true);
    }

    private void iconLoaded(Image icon) {
        this.icon = icon;
        repaint();
    }

    @Override
    public int getIconWidth() {
        return size == null ? 0 : size.width;
    }

    @Override
    public int getIconHeight() {
        return size == null ? 0 : size.height;
    }

    private final DisabledImageIcon disabledInstance = new DisabledImageIcon(this);

    @Override
    public DisabledImageIcon getDisabledInstance() {
        return disabledInstance;
    }

    private class IconLoader implements Runnable {
        private final Future<Image> task;
        private final Dimension size;

        IconLoader(Future<Image> task, Dimension size) {
            this.task = Objects.requireNonNull(task);
            this.size = size;
        }

        @Override
        public void run() {
            Image icon;

            try {
                icon = task.get();
            } catch (InterruptedException interruptedException) {
                LOGGER.warn("Interrupted while waiting for icon");
                return;
            } catch (ExecutionException e) {
                LOGGER.warn("Icon loading failed", e);
                return;
            }

            final Image result;

            if (MultiResInterface.INSTANCE.isEnabled()) {
                result = toMultiResIcon(icon, size.width, size.height);
            } else {
                result = scaledIcon(icon, size.width, size.height, 1.0);
            }

            if (DelayedIcon.this.loader == this) {
                SwingUtil.later(() -> iconLoaded(result));
            } else {
                LOGGER.debug("Icon loader result discarded");
            }
        }
    }

    private static Dimension computeSize(
            int iconWidth, int iconHeight,
            int targetWidth, int targetHeight) {
        if (targetWidth == 0 && targetHeight == 0) {
            return new Dimension(iconWidth, iconHeight);
        }

        double ratio = (double) iconWidth / iconHeight;
        int width, height;

        if (targetHeight == 0) {
            width = targetWidth;
            height = (int) (targetWidth / ratio);
        } else if (targetWidth == 0) {
            width = (int) (targetHeight * ratio);
            height = targetHeight;
        } else {
            width = targetWidth;
            height = targetHeight;
        }

        return new Dimension(width, height);
    }

    private static Image scaledIcon(Image icon, int width, int height, double scale) {
        final int hints = Image.SCALE_SMOOTH;
        if (scale == 1.0) {
            return icon.getScaledInstance(width, height, hints);
        } else {
            return icon.getScaledInstance((int) (scale * width), (int) (scale * height), hints);
        }
    }

    private static Image toMultiResIcon(Image icon, int width, int height) {
        return MultiResInterface.INSTANCE.createImage(
                scaledIcon(icon, width, height, 1.0),
                scaledIcon(icon, width, height, SwingUtil.getScalingFactor())
        );
    }
}
