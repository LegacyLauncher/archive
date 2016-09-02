package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.converter.LocaleConverter;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
            LocalizableHTMLLabel label = new LocalizableHTMLLabel("firstrun.notice.body." + i);
            label.setLabelWidth(getBodyText().getLabelWidth());
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

        final ExtendedComboBox<Locale> localeChoose = new ExtendedComboBox<Locale>(new LocaleConverter() {
            @Override
            public String toString(Locale from) {
                return toString(from, Locale.US);
            }
        });
        for (Locale locale : t.getLang().getLocales()) {
            localeChoose.addItem(locale);
        }
        localeChoose.setSelectedValue(t.getLang().getSelected());
        localeChoose.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Locale selected = localeChoose.getSelectedValue();
                if (selected == null) {
                    selected = Locale.US;
                }
                t.getSettings().set("locale", selected);
                t.getLang().setSelected(selected);
                updateLocale();
                t.showTranslationContributors();
            }
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
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        getFooter().add(yesButton, c);

        updateLocale();
        pack();

        t.showTranslationContributors();
    }
}
