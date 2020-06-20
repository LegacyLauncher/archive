package ru.turikhay.tlauncher.ui.frames.yandex;

import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedCheckbox;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YandexFrame extends VActionFrame {

    private final ExtendedCheckbox
            downloadYandexBrowser,
            downloadShortcuts,
            downloadAddons,
            downloadBrowserManager;
    private boolean installRequested;

    public YandexFrame() {
        //setResizable(false);
        setTitle("Установка TLauncher");

        getHead().setText("");
        getHead().setIcon(Images.getIcon("logo_yandex.png", SwingUtil.magnify(404 / 4), SwingUtil.magnify(220 / 4)));


        getBodyText().setText("TLauncher рекомендует вам установить программы, расширения и настройки Яндекса для комфортной работы в интернете. <a href=\"https://yandex.ru/soft/distribution/\">Узнать подробности »</a>");
        getBody().add(Box.createRigidArea(SwingUtil.magnify(new Dimension(1, 4))));
        getBody().add(downloadYandexBrowser = new ExtendedCheckbox("Загрузить и установить Яндекс.Браузер", true));
        getBody().add(downloadShortcuts = new ExtendedCheckbox("Загрузить и установить настройки быстрого доступа к поиску и сервисам Яндекса", true));
        getBody().add(downloadAddons = new ExtendedCheckbox("Установить расширения Яндекса для браузеров", true));
        getBody().add(downloadBrowserManager = new ExtendedCheckbox("Установить Менеджер браузеров", true));
        getBody().add(Box.createRigidArea(SwingUtil.magnify(new Dimension(1, 6))));
        getBody().add(new EditorPane("Устанавливая рекомендуемое программное обеспечение, вы соглашаетесь с лицензионными соглашениями <a href=\"https://yandex.ru/legal/browser_agreement/\">Яндекс.Браузера</a> и <a href=\"https://yandex.ru/legal/desktop_software_agreement/\">настольного ПО Яндекса</a>", SwingUtil.magnify(labelWidth)) {
            {
                setAlignmentX(LEFT_ALIGNMENT);
            }
        });
        getBody().add(Box.createRigidArea(SwingUtil.magnify(new Dimension(1, 6))));


        getFooter().setLayout(new GridBagLayout());

        ExtendedPanel panel = new ExtendedPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 0; fc.weightx = 1.0; fc.weighty = 1.0;
        fc.fill = GridBagConstraints.BOTH;

        fc.gridy++;
        getFooter().add(panel, fc);

        /*fc.gridy++; fc.insets = SwingUtil.magnify(new Insets(5, 0, 0, 0));
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPreferredSize(SwingUtil.magnify(new Dimension(labelWidth, 15)));
        getFooter().add(progressBar, fc);*/

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = -1;

        c.gridx++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        ExtendedButton moreButton = new ExtendedButton("Отмена");
        moreButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
        moreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panel.add(moreButton, c);

        c.gridx++;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new ExtendedPanel(), c);

        c.gridx++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        final ExtendedButton yesButton = new ExtendedButton("Установить");
        yesButton.setFont(yesButton.getFont().deriveFont(Font.BOLD));
        yesButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                installRequested = true;
                dispose();
            }
        });
        panel.add(yesButton, c);


        ItemListener checkboxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(!getOfferSelection().isEmpty()) {
                    yesButton.setText("Установить");
                    yesButton.setFont(yesButton.getFont().deriveFont(Font.BOLD));
                } else {
                    yesButton.setText("Продолжить");
                    yesButton.setFont(yesButton.getFont().deriveFont(Font.PLAIN));
                }
            }

            private int selected(ExtendedCheckbox checkbox) {
                return downloadYandexBrowser.isSelected() ? 1 : 0;
            }
        };
        downloadYandexBrowser.addListener(checkboxListener);
        downloadShortcuts.addListener(checkboxListener);
        downloadAddons.addListener(checkboxListener);
        downloadBrowserManager.addListener(checkboxListener);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                YandexInstaller.getInstance().start(installRequested? getOfferSelection() : Collections.EMPTY_LIST);
            }
        });
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();

        yesButton.requestFocus();
    }

    private List<YandexOffer> getOfferSelection() {
        return new ArrayList<YandexOffer>() {
            {
                addIf(YandexOffer.BROWSER, downloadYandexBrowser);
                addIf(YandexOffer.SHORTCUTS, downloadShortcuts);
                addIf(YandexOffer.ADDONS, downloadAddons);
                addIf(YandexOffer.BROWSER_MANAGER, downloadBrowserManager);
            }

            private void addIf(YandexOffer offer, ExtendedCheckbox checkbox) {
                if(checkbox.isSelected()) {
                    add(offer);
                }
            }
        };
    }

    @Override
    public void updateLocale() {
    }
}
