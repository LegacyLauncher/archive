package ru.turikhay.tlauncher.ui.notice;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.swing.*;
import java.util.List;

public abstract class NoticeAction {
    private final String type;
    protected final String L10N_PREFIX;

    NoticeAction(String type) {
        this.type = type;
        L10N_PREFIX = "notice.action." + type + ".";
    }

    abstract List<? extends JMenuItem> getMenuItemList();

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public final String toString() {
        return toStringBuilder().build();
    }
}