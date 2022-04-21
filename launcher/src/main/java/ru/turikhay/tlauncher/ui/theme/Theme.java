package ru.turikhay.tlauncher.ui.theme;

import ru.turikhay.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public abstract class Theme {
    private static Theme theme;

    public static Theme getTheme() {
        if (theme == null) {
            theme = SystemTheme.getSystemTheme();
        }
        return theme;
    }

    public static Theme loadTheme(String name, InputStream in) throws IOException {
        return (theme = new ExternalTheme(name, in));
    }

    public static void setup(JComponent comp) {
        Objects.requireNonNull(comp);
        comp.setForeground(getTheme().getForeground());
        comp.setBackground(getTheme().getBackground());
    }

    Theme(String name) {
        String name1 = StringUtil.requireNotBlank(name, "name");
    }

    public abstract Color getForeground();

    public abstract Color getSemiForeground();

    public abstract Color getBackground();

    public abstract Color getPanelBackground();

    public abstract Color getSuccess();

    public abstract Color getFailure();

    public abstract int getBorderSize();

    public abstract Color getBorder(Border border);

    public abstract Color getShadow(Border border);

    public abstract int getArc(Border border);

    public abstract Color getIconColor(String iconName);

    public abstract boolean useDarkTheme();

    public enum Border {
        MAIN_PANEL,
        ADDITIONAL_PANEL,
        SETTINGS_PANEL;

        public int id() {
            return ordinal();
        }
    }
}
