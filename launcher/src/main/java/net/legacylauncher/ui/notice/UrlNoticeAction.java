package net.legacylauncher.ui.notice;

import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.StringUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UrlNoticeAction extends NoticeAction {
    private final String name;
    private final URL url;

    UrlNoticeAction(String name, URL url) {
        super("url");

        this.name = StringUtil.requireNotBlank(name, "name");
        this.url = Objects.requireNonNull(url, "url");
    }

    @Override
    Runnable getRunnable() {
        return this::openLink;
    }

    @Override
    List<? extends JMenuItem> getMenuItemList() {
        List<LocalizableMenuItem> list = new ArrayList<>();

        LocalizableMenuItem item = new LocalizableMenuItem(L10N_PREFIX + "open", name);
        item.addActionListener(e -> openLink());
        list.add(item);

        return list;
    }

    private void openLink() {
        OS.openLink(url);
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("name", name).append("url", url);
    }
}
