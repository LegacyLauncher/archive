package ru.turikhay.tlauncher.ui.swing.combobox;

import ru.turikhay.tlauncher.ui.images.ImageIcon;

import javax.annotation.Nullable;

public class IconText {
    public static final IconText EMPTY = new IconText(null, "");

    private final @Nullable ImageIcon icon;
    private final String text;

    public IconText(@Nullable ImageIcon icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    @Nullable
    public ImageIcon getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }
}
