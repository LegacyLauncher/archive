package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ContactUsFrame extends VActionFrame {

    private static final List<SupportService> supportServices = Arrays.asList(
            cisOnly("vk", "https://tlaun.ch/support/vk"),
            cisOnly("discord", "https://tlaun.ch/support/discord/ru"),
            any("facebook", "https://tlaun.ch/support/fb"),
            notCis("discord", "https://tlaun.ch/support/discord/intl"),
            any("mail", "mailto:" + TLauncher.getSupportEmail())
    );

    public ContactUsFrame() {
        setTitlePath("support.contact.title");
        getHead().setIcon(Images.getIcon("pencil.png", SwingUtil.magnify(32)));
        getHead().setText("support.contact.title");
        getBodyText().setText("support.contact.body");

        getFooter().removeAll();
        getFooter().setLayout(new GridBagLayout());

        GridBagConstraints c;
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;

        for (int i = 0; i < supportServices.size(); i++) {
            SupportService supportService = supportServices.get(i);
            if (!supportService.isApplicable()) {
                continue;
            }
            if(i == supportServices.size() - 1) {
                c.gridwidth = GridBagConstraints.REMAINDER;
            }
            getFooter().add(supportService.createButton(), c);
            if(++c.gridx % 2 == 0) {
                c.gridx = 0;
                c.gridy++;
            }
        }

        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private interface LangValidator {
        LangValidator ONLY_CIS = new LangValidator() {
            @Override
            public boolean isValidFor(String locale) {
                return Configuration.isUSSRLocale(locale);
            }
        };
        LangValidator NOT_CIS = new LangValidator() {
            @Override
            public boolean isValidFor(String locale) {
                return !ONLY_CIS.isValidFor(locale);
            }
        };
        LangValidator ANY = new LangValidator() {
            @Override
            public boolean isValidFor(String locale) {
                return true;
            }
        };

        boolean isValidFor(String locale);
    }

    private static class SupportService {
        final String name;
        final URL url;
        final LangValidator lang;

        SupportService(String name, String url, LangValidator lang) {
            this.name = name;
            this.url = U.makeURL(url, true);
            this.lang = lang;
        }

        LocalizableButton createButton() {
            LocalizableButton b = new LocalizableButton("support.contact.buttons." + name);
            b.setIcon(Images.getIcon(name + ".png", SwingUtil.magnify(24)));
            b.setPreferredSize(new Dimension(1, SwingUtil.magnify(50)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    OS.openLink(url);
                }
            });
            return b;
        }

        boolean isApplicable() {
            return lang.isValidFor(TLauncher.getInstance().getSettings().getLocale().toString());
        }
    }

    static SupportService cisOnly(String name, String url) {
        return new SupportService(name, url, LangValidator.ONLY_CIS);
    }

    static SupportService notCis(String name, String url) {
        return new SupportService(name, url, LangValidator.NOT_CIS);
    }

    static SupportService any(String name, String url) {
        return new SupportService(name, url, LangValidator.ANY);
    }
}
