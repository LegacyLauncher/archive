package net.legacylauncher.ui.support;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.BuildConfig;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.ui.frames.VActionFrame;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.U;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ContactUsFrame extends VActionFrame {

    private static final List<SupportService> supportServices = Arrays.asList(
            cisOnly("vk", "https://llaun.ch/support/vk"),
            cisOnly("discord", "https://llaun.ch/support/discord/ru"),
            notCis("discord", "https://llaun.ch/support/discord/intl"),
            any("mail", "envelope-open", "mailto:" + BuildConfig.SUPPORT_EMAIL)
    );

    public ContactUsFrame() {
        setTitlePath("support.contact.title");
        getHead().setIcon(Images.getIcon32("pencil-square"));
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
            if (i == supportServices.size() - 1) {
                c.gridwidth = GridBagConstraints.REMAINDER;
            }
            getFooter().add(supportService.createButton(), c);
            if (++c.gridx % 2 == 0) {
                c.gridx = 0;
                c.gridy++;
            }
        }

        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private interface LangValidator {
        LangValidator ONLY_CIS = Configuration::isLikelyRussianSpeakingLocale;
        LangValidator NOT_CIS = locale -> !ONLY_CIS.isValidFor(locale);
        LangValidator ANY = locale -> true;

        boolean isValidFor(String locale);
    }

    private static class SupportService {
        final String name, icon;
        final URL url;
        final LangValidator lang;

        SupportService(String name, String icon, String url, LangValidator lang) {
            this.name = name;
            this.icon = icon;
            this.url = U.makeURL(url, true);
            this.lang = lang;
        }

        LocalizableButton createButton() {
            LocalizableButton b = new LocalizableButton("support.contact.buttons." + name);
            b.setIcon(Images.getIcon24(icon));
            b.setPreferredSize(new Dimension(1, SwingUtil.magnify(50)));
            b.addActionListener(e -> OS.openLink(url));
            return b;
        }

        boolean isApplicable() {
            return lang.isValidFor(LegacyLauncher.getInstance().getSettings().getLocale().toString());
        }
    }

    static SupportService cisOnly(String name, String url) {
        return new SupportService(name, "logo-" + name, url, LangValidator.ONLY_CIS);
    }

    static SupportService notCis(String name, String url) {
        return new SupportService(name, "logo-" + name, url, LangValidator.NOT_CIS);
    }

    static SupportService any(String name, String icon, String url) {
        return new SupportService(name, icon, url, LangValidator.ANY);
    }

    static SupportService any(String name, String url) {
        return new SupportService(name, "logo-" + name, url, LangValidator.ANY);
    }
}
