package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class DirectNoticeImage extends NoticeImage {
    private final Image image;

    DirectNoticeImage(Image image) {
        this.image = Objects.requireNonNull(image, "image");
    }

    @Override
    public int getWidth() {
        return image.getWidth(null);
    }

    @Override
    public int getHeight() {
        return image.getHeight(null);
    }

    @Override
    public Future<Image> getTask() {
        return CompletableFuture.completedFuture(image);
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("image", image).append("width", getWidth()).append("height", getHeight());
    }
}
