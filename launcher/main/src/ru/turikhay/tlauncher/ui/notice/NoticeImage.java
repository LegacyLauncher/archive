package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.ui.images.ExtendedIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class NoticeImage {
    public static NoticeImage DEFAULT_IMAGE = new UrlNoticeImage(Images.getRes("fav128.png"));
    static HashMap<String, NoticeImage> definedImages = new HashMap<String, NoticeImage>(){
        {
            put("default", DEFAULT_IMAGE);
        }
    };

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

    protected String logPrefix = '[' + getClass().getSimpleName() + ']';
    private void log(Object...o) {
        U.log(logPrefix, o);
    }
}