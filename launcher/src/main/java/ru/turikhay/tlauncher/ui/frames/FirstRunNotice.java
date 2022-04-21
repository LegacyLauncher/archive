package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.converter.LocaleConverter;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.support.ContributorsAlert;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

public class FirstRunNotice extends VActionFrame {
    private final TLauncher t;

    public FirstRunNotice() {
        super(SwingUtil.magnify(500));
        t = TLauncher.getInstance();

        setTitlePath("firstrun.title");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        getHead().setText("firstrun.notice.welcome");

        VPanel list = new VPanel();
        list.setInsets(new MagnifiedInsets(0, 10, 0, 0));
        for (int i = 0; i < 3; i++) {
            VActionFrame.VActionBody label = new VActionFrame.VActionBody();
            label.setText("firstrun.notice.body." + i);
            //label.setLabelWidth(getLabelWidth());
            list.add(label);
        }

        getBodyText().setText("firstrun.notice.body");
        getBody().add(list);

        getFooter().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = -1;

        c.gridx++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;

        final ExtendedComboBox<Locale> localeChoose = new ExtendedComboBox<>(new LocaleConverter() {
            @Override
            public String toString(Locale from) {
                return toString(from, Locale.US);
            }
        });
        for (Locale locale : LangConfiguration.getAvailableLocales()) {
            localeChoose.addItem(locale);
        }
        localeChoose.setSelectedValue(t.getLang().getLocale());
        localeChoose.addItemListener(e -> {
            Locale selected = localeChoose.getSelectedValue();
            if (selected == null) {
                selected = Locale.US;
            }
            t.getSettings().set("locale", selected);
            t.getLang().setLocale(selected);
            updateLocale();
            ContributorsAlert.showAlert();
        });

        getFooter().add(localeChoose, c);

        c.gridx++;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        getFooter().add(new ExtendedPanel(), c);

        c.gridx++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        LocalizableButton yesButton = new LocalizableButton("firstrun.notice.answer.yes");
        yesButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
        yesButton.addActionListener(e -> dispose());
        getFooter().add(yesButton, c);

        updateLocale();
        pack();

        ContributorsAlert.showAlert();
    }

    private FirstRunNotice parent;
    private boolean localeUpdated;

    private void showLocaleUpdated(final FirstRunNotice parent) {
        this.parent = parent;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!localeUpdated) {
                    FirstRunNotice p = FirstRunNotice.this;
                    while (p.parent != null) {
                        p = p.parent;
                    }
                    p.localeUpdated = false;
                }
            }
        });
        showAtCenter();
    }

    public void showAndWait() {
        showAtCenter();
        while (isDisplayable() || localeUpdated) {
            U.sleepFor(100);
        }
    }

    @Override
    public void updateLocale() {
        if (isDisplayable()) {
            localeUpdated = true;
            dispose();
            FirstRunNotice f = new FirstRunNotice();
            f.showLocaleUpdated(this);
        }
    }
}
