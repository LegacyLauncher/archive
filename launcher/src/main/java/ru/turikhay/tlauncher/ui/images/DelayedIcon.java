package ru.turikhay.tlauncher.ui.images;

import ru.turikhay.tlauncher.ui.notice.NoticeImage;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class DelayedIcon extends ExtendedLabel implements ExtendedIcon {
    private Image image, i;
    private int imageWidth, imageHeight, width, height;
    private DelayedIconLoader loader;

    public DelayedIcon() {
        setIcon(this);
        setDisabledIcon(getDisabledInstance());
    }

    public DelayedIcon(NoticeImage noticeImage, int width, int height) {
        this();
        setImage(noticeImage.getTask(), noticeImage.getWidth(), noticeImage.getHeight(), width, height);
    }

    public void setImage(Image image, int width, int height) {
        this.image = image;
        this.imageWidth = image == null ? 0 : image.getWidth(null);
        this.imageHeight = image == null ? 0 : image.getHeight(null);
        setIconSize(width, height);
    }

    public void setImage(Future<Image> image, int imageWidth, int imageHeight, int width, int height) {
        this.image = null;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        setIconSize(width, height);

        AsyncThread.future(loader = new DelayedIconLoader(image));
    }

    public Dimension getIconSize() {
        return new Dimension(width, height);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    @Override
    public DisabledImageIcon getDisabledInstance() {
        return new DisabledImageIcon(this);
    }

    @Override
    public void setIconSize(int width, int height) {
        Dimension d = getSize(imageWidth, imageHeight, width, height);

        this.width = d.width;
        this.height = d.height;

        if (image == null) {
            i = null;
        } else {
            i = image.getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH);
        }

        repaint();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (i != null) {
            g.drawImage(i, x, y, null);
        }
    }

    private static Dimension getSize(int originalWidth, int originalHeight, int targetWidth, int targetHeight) {
        if (targetWidth == 0 && targetHeight == 0) {
            return new Dimension(originalWidth, originalHeight);
        }

        double ratio = (double) originalWidth / originalHeight;
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

    private class DelayedIconLoader implements Callable<Void> {
        private Future<Image> imageTask;

        DelayedIconLoader(Future<Image> image) {
            this.imageTask = image;
        }

        @Override
        public Void call() throws Exception {
            Image image = imageTask.get();

            if (this == loader) {
                setImage(image, width, height);
            }

            return null;
        }
    }
}
