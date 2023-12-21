package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.StringUtil;

import java.util.Objects;

public final class Notice {
    private final int id, pos;
    private final String text, erid;
    private final NoticeImage image;
    private final NoticeAction action;

    private boolean promoted;

    Notice(int id, int pos, String text, String erid, NoticeImage image, NoticeAction action) {
        this.id = id;
        this.pos = pos;
        this.text = StringUtil.requireNotBlank(text, "text");
        this.erid = erid;
        this.image = Objects.requireNonNull(image, "image");
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

    public String getErid() {
        return erid;
    }

    public NoticeImage getImage() {
        return image;
    }

    public NoticeAction getAction() {
        return action;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public void setPromoted(boolean promoted) {
        this.promoted = promoted;
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("pos", pos)
                .append("text", text.length() > 30 ? text.substring(0, 27) + "..." : text)
                .append("erid", erid)
                .append("image", image)
                .append("action", action)
                .build();
    }
}
