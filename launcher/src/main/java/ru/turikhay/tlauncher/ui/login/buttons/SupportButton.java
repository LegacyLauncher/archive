package ru.turikhay.tlauncher.ui.login.buttons;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.support.PreSupportFrame;
import ru.turikhay.tlauncher.ui.swing.DelayedComponent;
import ru.turikhay.tlauncher.ui.swing.DelayedComponentLoader;
import ru.turikhay.util.OS;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class SupportButton extends LocalizableButton implements Blockable {

    //private ProcessFrame<Void> dxdiagFlusher;
    private DelayedComponent<PreSupportFrame> supportFrame;

    private final ActionListener showSupportFrame = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadSupportFrame();
            if (!supportFrame.get().isVisible()) {
                supportFrame.get().showAtCenter();
            }
            /*dxdiagFlusher.submit(dxdiagFlusher.new Process() {
                @Override
                protected Void get() throws Exception {
                    if (DxDiag.isScannable()) {
                        try {
                            DxDiag.get();
                        } catch (Exception e) {
                            U.log("Could not retrieve DxDiag", e);
                        }
                    }
                    return null;
                }
            });*/
        }
    };

    private final HashMap<String, SupportMenu> localeMap = new HashMap<>();

    {
        localeMap.put("ru_RU", new SupportMenu("info-circle")
                .add("loginform.button.support.vk", Images.getIcon16("logo-vk"), actionURL("https://llaun.ch/vk"))
                .add("loginform.button.support.discord", Images.getIcon16("logo-discord"), actionURL("https://llaun.ch/discord/ru"))
                .addSeparator()
                .add("loginform.button.support", Images.getIcon16("life-ring"), showSupportFrame)
        );

        localeMap.put("uk_UA", new SupportMenu("info-circle")
                .add("loginform.button.support.discord", Images.getIcon16("logo-discord"), actionURL("https://llaun.ch/discord/ru"))
                .addSeparator()
                .add("loginform.button.support", Images.getIcon16("life-ring"), showSupportFrame)
        );

        localeMap.put("en_US", new SupportMenu("comments-o")
                .add("loginform.button.support.discord", Images.getIcon16("logo-discord"), actionURL("https://llaun.ch/discord/intl"))
                .addSeparator()
                .add("loginform.button.support", Images.getIcon16("life-ring"), showSupportFrame)
        );
    }

    SupportMenu menu;

    SupportButton(LoginForm loginForm) {
        setToolTipText("loginform.button.support");
        addActionListener(e -> {
            if (menu != null) {
                menu.showPopup();
            }
        });
        updateLocale();
    }

    private void loadSupportFrame() {
        PreSupportFrame oldSupportFrame = supportFrame == null ? null : supportFrame.get();
        supportFrame = new DelayedComponent<>(new DelayedComponentLoader<PreSupportFrame>() {
            @Override
            public PreSupportFrame loadComponent() {
                return new PreSupportFrame();
            }

            @Override
            public void onComponentLoaded(PreSupportFrame loaded) {
                TLauncher.getInstance().reloadLocale();
            }
        });

        if (oldSupportFrame != null && oldSupportFrame.isVisible()) {
            oldSupportFrame.dispose();
            supportFrame.get().showAtCenter();
        }
    }

    void setLocale(String locale) {
        if (menu != null) {
            menu.popup.setVisible(false);
        }

        menu = localeMap.get(locale);

        if (menu == null) {
            setIcon(null);
            setEnabled(false);
        } else {
            setIcon(menu.icon);
            setEnabled(true);
        }
    }

    public void block(Object reason) {
    }

    public void unblock(Object reason) {
    }

    public void updateLocale() {
        super.updateLocale();

        /*dxdiagFlusher = new ProcessFrame<Void>() {
            {
                setTitlePath("loginform.button.support.processing.title");
                getHead().setText("loginform.button.support.processing.head");
                setIcon("comments-o.png");
                pack();
            }

            protected void onSucceeded(Process process, Void result) {
                super.onSucceeded(process, result);
                supportFrame.showAtCenter();
            }

            protected void onCancelled() {
                super.onCancelled();
                DxDiag.cancel();
            }
        };*/

        if (supportFrame != null && supportFrame.isLoaded()) {
            loadSupportFrame();
        }

        String selectedLocale = TLauncher.getInstance().getSettings().getLocale().toString();
        String newLocale = null;

        for (String locale : localeMap.keySet())
            if (locale.equals(selectedLocale)) {
                newLocale = locale;
                break;
            }

        if (newLocale == null) {
            if (Configuration.isLikelyRussianSpeakingLocale(selectedLocale)) {
                newLocale = "ru_RU";
            }  else {
                newLocale = "en_US";
            }
        }

        setLocale(newLocale);
    }

    private class SupportMenu {
        final ImageIcon icon;
        final JPopupMenu popup = new JPopupMenu();

        SupportMenu(String icon) {
            this.icon = Images.getIcon24(icon);
        }

        void showPopup() {
            Localizable.updateContainer(popup);
            popup.show(SupportButton.this, 0, getHeight());
        }

        SupportMenu add(JMenuItem item) {
            popup.add(item);
            return this;
        }

        public SupportMenu add(String key, ImageIcon icon, ActionListener listener) {
            LocalizableMenuItem item = new LocalizableMenuItem(key);
            item.setIcon(icon);
            if (listener != null) item.addActionListener(listener);
            add(item);
            return this;
        }

        public SupportMenu add(String key, ActionListener listener) {
            return add(key, null, listener);
        }

        public SupportMenu add(String key) {
            LocalizableMenuItem item = new LocalizableMenuItem(key);
            item.setEnabled(false);
            add(item);
            return this;
        }

        public SupportMenu addSeparator() {
            popup.addSeparator();
            return this;
        }
    }

    private static ActionListener actionURL(String rawURL) {
        URL tryURL;

        try {
            tryURL = new URL(rawURL);
        } catch (MalformedURLException muE) {
            throw new RuntimeException(muE);
        }

        final URL url = tryURL;

        return e -> OS.openLink(url);
    }

    private static ActionListener actionAlert(final String msgPath, final Object textArea) {
        return e -> Alert.showLocMessage(msgPath, textArea);
    }
}
