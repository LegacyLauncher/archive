package ru.turikhay.tlauncher.ui.login.buttons;

import ru.turikhay.tlauncher.TLauncher;
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
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

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

    private final HashMap<String, SupportMenu> localeMap = new HashMap<String, SupportMenu>();

    {
        localeMap.put("ru_RU", new SupportMenu("info.png")
                .add("loginform.button.support.vk", Images.getIcon("vk.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/vk?from=menu"))
                .add("loginform.button.support.discord", Images.getIcon("discord.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/discord/ru?from=menu"))
                .addSeparator()
                .add("loginform.button.support", Images.getIcon("comments-o.png", SwingUtil.magnify(16)), showSupportFrame)
        );

        localeMap.put("uk_UA", new SupportMenu("comments-o.png")
                .add("loginform.button.support.vk", Images.getIcon("vk.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/vk?from=menu"))
                .add("loginform.button.support.discord", Images.getIcon("discord.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/discord/ru?from=menu"))
                .add("loginform.button.support.fb", Images.getIcon("facebook.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/fb?from=menu"))
                .addSeparator()
                .add("loginform.button.support", Images.getIcon("comments-o.png", SwingUtil.magnify(16)), showSupportFrame)
        );

        localeMap.put("en_US", new SupportMenu("comments-o.png")
                .add("loginform.button.support.discord", Images.getIcon("discord.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/discord/intl?from=menu"))
                .add("loginform.button.support.fb", Images.getIcon("facebook.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/fb?from=menu"))
                .addSeparator()
                .add("loginform.button.support", Images.getIcon("comments-o.png", SwingUtil.magnify(16)), showSupportFrame)
        );
    }

    SupportMenu menu;

    SupportButton(LoginForm loginForm) {
        setToolTipText("loginform.button.support");
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (menu != null) {
                    menu.showPopup();
                }
            }
        });
        updateLocale();
    }

    private void loadSupportFrame() {
        PreSupportFrame oldSupportFrame = supportFrame == null? null : supportFrame.get();
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

        if(supportFrame != null && supportFrame.isLoaded()) {
            loadSupportFrame();
        }

        String selectedLocale = TLauncher.getInstance().getSettings().getLocale().toString();
        String newLocale = "en_US";

        for (String locale : localeMap.keySet())
            if (locale.equals(selectedLocale)) {
                newLocale = locale;
                break;
            }

        setLocale(newLocale);
    }

    private class SupportMenu {
        final ImageIcon icon;
        final JPopupMenu popup = new JPopupMenu();

        SupportMenu(String icon) {
            this.icon = Images.getScaledIcon(icon, 20);
        }

        void showPopup() {
            Localizable.updateContainer(popup);
            popup.show(SupportButton.this, 0, getHeight());
        }

        SupportMenu add(JMenuItem item) {
            popup.add(item);
            return this;
        }

        public SupportMenu add(final String[] russianTitles, final String key, ImageIcon icon, ActionListener listener) {
            LocalizableMenuItem item = new LocalizableMenuItem(null) {
                @Override
                public void updateLocale() {
                    super.updateLocale();
                    if(TLauncher.getInstance() != null && "ru_RU".equals(TLauncher.getInstance().getSettings().getLocale().toString())) {
                        setText(U.getRandom(russianTitles));
                    } else {
                        setText(key);
                    }
                }
            };
            item.setIcon(icon);
            if (listener != null) item.addActionListener(listener);
            add(item);
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

        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.openLink(url);
            }
        };
    }

    private static ActionListener actionAlert(final String msgPath, final Object textArea) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Alert.showLocMessage(msgPath, textArea);
            }
        };
    }
}
