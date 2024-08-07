package net.legacylauncher.ui.loc;

import net.legacylauncher.ui.theme.Theme;

import javax.swing.*;
import java.awt.event.ItemListener;

public class LocalizableRadioButton extends JRadioButton implements LocalizableComponent {
    private static final long serialVersionUID = 1L;
    private String path;

    public LocalizableRadioButton() {
        init();
    }

    public LocalizableRadioButton(String path) {
        init();
        setLabel(path);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setLabel(String path) {
        setText(path);
    }

    public void setText(String path) {
        this.path = path;
        super.setText(Localizable.get() == null ? path : Localizable.get().get(path));
    }

    public String getLangPath() {
        return path;
    }

    public void addListener(ItemListener l) {
        super.getModel().addItemListener(l);
    }

    public void removeListener(ItemListener l) {
        super.getModel().removeItemListener(l);
    }

    public void updateLocale() {
        setLabel(path);
    }

    private void init() {
        setOpaque(false);
    }

    @Override
    public void updateUI() {
        Theme.setup(this);
        super.updateUI();
    }
}
