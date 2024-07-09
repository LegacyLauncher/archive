package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExtendedButton extends JButton {
    private static final long serialVersionUID = -2009736184875993130L;

    public ExtendedButton() {
        init();
    }

    public ExtendedButton(Icon icon) {
        super(icon);
        init();
    }

    public ExtendedButton(String text) {
        super(text);
        init();
    }

    public ExtendedButton(Action a) {
        super(a);
        init();
    }

    public ExtendedButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public void setIcon(Icon icon) {
        super.setIcon(icon);
        if (icon instanceof net.legacylauncher.ui.images.ImageIcon) {
            super.setDisabledIcon(((ImageIcon) icon).getDisabledInstance());
        }

    }

    private void init() {
        setFont(getFont().deriveFont(LegacyLauncherFrame.getFontSize()));
        setOpaque(false);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Component parent = findRootParent(getParent());
                if (parent != null) {
                    parent.requestFocusInWindow();
                }
            }

            private Component findRootParent(Component comp) {
                return comp == null ? null : (comp.getParent() == null ? comp : findRootParent(comp.getParent()));
            }
        });
    }

    @Override
    public void updateUI() {
        setForeground(Theme.getTheme().getForeground());
        setBackground(Theme.getTheme().getBackground());
        Icon icon = getIcon();
        if (icon instanceof JComponent) {
            ((JComponent) icon).updateUI();
        }
        super.updateUI();
    }
}
