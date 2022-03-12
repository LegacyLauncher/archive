package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.async.AsyncThread;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URL;
import java.util.concurrent.Future;

public class UrlNoticeImage extends NoticeImage {
    private final URL url;
    private int width, height;
    private Future<Image> future;

    UrlNoticeImage(URL url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    UrlNoticeImage(URL url) {
        this.url = url;
        Image image;
        try {
            image = getTask().get();
        } catch (Exception e) {
            return;
        }
        width = image.getWidth(null);
        height = image.getHeight(null);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Future<Image> getTask() {
        if (future == null) {
            return future = AsyncThread.future(() -> ImageIO.read(url));
        } else {
            return future;
        }
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("url", url)
                .append("width", width)
                .append("height", height);
    }
}
