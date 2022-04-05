package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.U;
import ru.turikhay.util.async.EmptyFuture;

import java.awt.*;
import java.util.concurrent.Future;

public class DirectNoticeImage extends NoticeImage {
    private final Image image;

    DirectNoticeImage(Image image) {
        this.image = U.requireNotNull(image, "image");
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
        return new EmptyFuture<Image>(image);
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("image", image).append("width", getWidth()).append("height", getHeight());
    }
}
