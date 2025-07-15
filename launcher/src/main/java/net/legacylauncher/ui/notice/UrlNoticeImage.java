package net.legacylauncher.ui.notice;

import net.legacylauncher.util.async.AsyncThread;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class UrlNoticeImage extends NoticeImage {
    private final URL url;
    private int width, height;
    private CompletableFuture<Image> future;

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
    public CompletableFuture<Image> getTask() {
        if (future == null) {
            return future = AsyncThread.completableFuture(() -> ImageIO.read(url));
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
