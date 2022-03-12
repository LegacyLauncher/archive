package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;

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
    List<? extends JMenuItem> getMenuItemList() {
        List<LocalizableMenuItem> list = new ArrayList<>();

        LocalizableMenuItem item = new LocalizableMenuItem(L10N_PREFIX + "open", name);
        item.addActionListener(e -> OS.openLink(url));
        list.add(item);

        return list;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("name", name).append("url", url);
    }
}
