package ru.turikhay.tlauncher.ui.notice;

import com.google.gson.JsonObject;

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
}
