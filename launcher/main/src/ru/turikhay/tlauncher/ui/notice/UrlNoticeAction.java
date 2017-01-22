package ru.turikhay.tlauncher.ui.notice;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UrlNoticeAction extends NoticeAction {
    private final String name;
    private final URL url;

    UrlNoticeAction(String name, URL url) {
        super("url");

        this.name = StringUtil.requireNotBlank(name, "name");
        this.url = U.requireNotNull(url,"url");
    }

    @Override
    List<? extends JMenuItem> getMenuItemList() {
        List<LocalizableMenuItem> list = new ArrayList<LocalizableMenuItem>();

        LocalizableMenuItem item = new LocalizableMenuItem(L10N_PREFIX + "open", name);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.openLink(url);
            }
        });
        list.add(item);

        return list;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("name", name).append("url", url);
    }
}
