package ru.turikhay.tlauncher.ui.notice;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class NoticePopup extends JPopupMenu {
    private final List<JMenuItem> registeredItems = new ArrayList<>();
    private Notice notice;

    public NoticePopup() {
    }

    public Notice getNotice() {
        return notice;
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
        updateList();
    }

    public void registerItem(JMenuItem item) {
        registeredItems.add(item);
        updateList();
    }

    public void updateList() {
        removeAll();

        if (notice == null) {
            return;
        }

        if (notice.getAction() == null) {
            return;
        }

        List<? extends JMenuItem> items = notice.getAction().getMenuItemList();
        if (items != null) {
            for (JMenuItem item : items) {
                add(item);
            }
        }
        if (!registeredItems.isEmpty()) {
            if (items != null) {
                addSeparator();
            }
            for (JMenuItem item : registeredItems) {
                add(item);
            }
        }
    }
}
