package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class Notice {
    private final int id, pos;
    private final String text;
    private final NoticeImage image;
    private final NoticeAction action;

    Notice(int id, int pos, String text, NoticeImage image, NoticeAction action) {
        this.id = id;
        this.pos = pos;
        this.text = StringUtil.requireNotBlank(text, "text");
        this.image = U.requireNotNull(image, "image");
        this.action = action;
    }

    public int getId() {
        return id;
    }

    public int getPos() {
        return pos;
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

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("pos", pos)
                .append("text", text.length() > 30? text.substring(0, 27) + "..." : text)
                .append("image", image)
                .append("action", action)
                .build();
    }
}
