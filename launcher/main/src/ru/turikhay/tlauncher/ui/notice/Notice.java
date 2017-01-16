package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class Notice {
    private final int id;
    private final String text;
    private final NoticeImage image;
    private final NoticeAction action;

    Notice(int id, String text, NoticeImage image, NoticeAction action) {
        this.id = id;
        this.text = StringUtil.requireNotBlank(text, "text");
        this.image = U.requireNotNull(image, "image");
        this.action = action;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public NoticeImage getImage() {
        return image;
    }

    public NoticeAction getAction() {
        return action;
    }
}
