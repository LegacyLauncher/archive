package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.ui.images.Images;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class NoticeImage {
    private static final Map<String, NoticeImage> definedImages = new HashMap<>();

    public static Map<String, NoticeImage> getDefinedImages() {
        if (definedImages.isEmpty()) {
            definedImages.put("default", new DirectNoticeImage(Images.loadIcon64("logo-tl")));
            definedImages.put("youtube", new DirectNoticeImage(Images.loadIcon64("logo-youtube")));
        }

        return definedImages;
    }

    NoticeImage() {
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract Future<Image> getTask();

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public final String toString() {
        return toStringBuilder().build();
    }
}
