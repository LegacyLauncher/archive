package net.legacylauncher.ui.logger;

import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.swing.EmptyAction;
import net.legacylauncher.ui.swing.TextPopup;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

class LoggerPopup extends TextPopup {
    private final LoggerFrame frame;
    private final EmptyAction clearAllAction;

    public LoggerPopup(LoggerFrame f) {
        this.frame = f;
        this.clearAllAction = new EmptyAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.clear();
            }
        };
    }

    protected JPopupMenu getPopup(MouseEvent e, JTextComponent comp) {
        JPopupMenu menu = super.getPopup(e, comp);
        if (menu == null) {
            return null;
        } else {
            menu.addSeparator();
            menu.add(clearAllAction).setText(Localizable.get("logger.clear.popup"));
            return menu;
        }
    }
}
