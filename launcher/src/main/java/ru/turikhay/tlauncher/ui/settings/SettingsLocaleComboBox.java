package ru.turikhay.tlauncher.ui.settings;

import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.converter.LocaleConverter;
import ru.turikhay.tlauncher.ui.editor.EditorComboBox;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.OS;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * Created by turikhay on 20.03.2016.
 */
public class SettingsLocaleComboBox extends BorderPanel implements EditorField, LocalizableComponent {
    final SettingsPanel panel;
    final EditorComboBox<Locale> comboBox;
    final ExtendedLabel hint;

    public SettingsLocaleComboBox(SettingsPanel panel) {
        this.panel = panel;

        comboBox = new EditorComboBox<>(new LocaleConverter(), LangConfiguration.getAvailableLocales().toArray(new Locale[0]));
        setCenter(comboBox);

        hint = new ExtendedLabel();
        hint.setFont(hint.getFont().deriveFont(hint.getFont().getSize() - 2.f));
        hint.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hint.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                OS.openLink("https://llaun.ch/l10n");
            }
        });
        setSouth(hint);

        updateLocale();
    }

    @Override
    public String getSettingsValue() {
        return comboBox.getSettingsValue();
    }

    @Override
    public void setSettingsValue(String var1) {
        comboBox.setSettingsValue(var1);
    }

    @Override
    public boolean isValueValid() {
        return comboBox.isValueValid();
    }

    @Override
    public void block(Object var1) {
        comboBox.setEnabled(false);
        hint.setEnabled(false);
    }

    @Override
    public void unblock(Object var1) {
        comboBox.setEnabled(true);
        hint.setEnabled(true);
    }

    private static final String CONTRIBUTE_PATH = "settings.lang.contribute";

    @Override
    public void updateLocale() {
        String locale = panel.lang.getLocale().toString();
        hint.setVisible(!locale.equals("ru_RU") && !locale.equals("uk_UA"));

        String hintLocalized = panel.lang.get(CONTRIBUTE_PATH);
        hint.setText("<html>" + hintLocalized + "</html>");
    }
}
