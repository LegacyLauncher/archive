package net.legacylauncher.ui.notice;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.LazyInitException;
import net.legacylauncher.util.StringUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
public final class Notice {
    public static final Dimension BANNER_DIMENSIONS = new Dimension(570, 64);

    @Getter
    private final int id;
    @Getter
    private final int pos;
    @Getter
    private final String text;
    @Getter
    private final String erid;
    @Getter
    private final String eridUrl;
    @Getter
    private final NoticeImage image;
    @Getter
    private final NoticeAction action;

    @Setter
    @Getter
    private boolean promoted;

    @Setter
    private Lazy<CompletableFuture<Image>> banner;

    Notice(int id, int pos, String text, String erid, String eridUrl, NoticeImage image, NoticeAction action) {
        this.id = id;
        this.pos = pos;
        this.text = StringUtil.requireNotBlank(text, "text");
        this.erid = erid;
        this.eridUrl = eridUrl;
        this.image = Objects.requireNonNull(image, "image");
        this.action = action;
    }

    public CompletableFuture<Image> getBanner() {
        if (banner == null) {
            return null;
        }
        try {
            return banner.get();
        } catch (LazyInitException e) {
            log.error("Failed to load banner for notice #{}", id, e);
            CompletableFuture<Image> f = new CompletableFuture<>();
            f.completeExceptionally(e);
            return f;
        }
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("pos", pos)
                .append("text", text.length() > 30 ? text.substring(0, 27) + "..." : text)
                .append("erid", erid)
                .append("eridUrl", eridUrl)
                .append("image", image)
                .append("action", action)
                .build();
    }
}
