package ru.turikhay.tlauncher.bootstrap.ui;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

class TextPopup extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.META_MASK) {
            Object source = e.getSource();
            if (source instanceof JTextComponent) {
                JPopupMenu popup = getPopup(e, (JTextComponent) source);
                if (popup != null) {
                    popup.show(e.getComponent(), e.getX(), e.getY() - popup.getSize().height);
                }
            }
        }
    }

    protected JPopupMenu getPopup(MouseEvent e, final JTextComponent comp) {
        if (!comp.isEnabled()) {
            return null;
        }

        if (!comp.isFocusOwner()) {
            comp.requestFocus();
        }

        boolean isEditable = comp.isEditable();
        boolean isSelected = comp.getSelectedText() != null;
        boolean hasValue = StringUtils.isNotEmpty(comp.getText());
        boolean pasteAvailable = isEditable && Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);
        JPopupMenu menu = new JPopupMenu();
        Action cut = isEditable ? selectAction(comp, "cut-to-clipboard", "cut") : null;
        final Action copy = selectAction(comp, "copy-to-clipboard", "copy");
        Action paste = pasteAvailable ? selectAction(comp, "paste-from-clipboard", "paste") : null;
        final Action selectAll = hasValue ? selectAction(comp, "select-all", "selectAll") : null;
        EmptyAction copyAll;
        if (selectAll != null && copy != null) {
            copyAll = new EmptyAction() {
                public void actionPerformed(ActionEvent e) {
                    selectAll.actionPerformed(e);
                    copy.actionPerformed(e);
                    comp.setSelectionStart(comp.getSelectionEnd());
                }
            };
        } else {
            copyAll = null;
        }

        if (cut != null) {
            menu.add(cut).setText("Cut");
        }

        if (isSelected && copy != null) {
            menu.add(copy).setText("Copy");
        }

        if (paste != null) {
            menu.add(paste).setText("Paste");
        }

        if (selectAll != null) {
            if (menu.getComponentCount() > 0 && !(menu.getComponent(menu.getComponentCount() - 1) instanceof JPopupMenu.Separator)) {
                menu.addSeparator();
            }

            menu.add(selectAll).setText("Select all");
        }

        if (copyAll != null) {
            menu.add(copyAll).setText("Copy all");
        }

        if (menu.getComponentCount() == 0) {
            return null;
        } else {
            if (menu.getComponent(0) instanceof JPopupMenu.Separator) {
                menu.remove(0);
            }

            if (menu.getComponent(menu.getComponentCount() - 1) instanceof JPopupMenu.Separator) {
                menu.remove(menu.getComponentCount() - 1);
            }

            return menu;
        }
    }

    public static Action selectAction(JTextComponent comp, String general, String fallback) {
        Action action = comp.getActionMap().get(general);
        if (action == null) {
            action = comp.getActionMap().get(fallback);
        }

        return action;
    }

    private static abstract class EmptyAction implements Action {
        private boolean enabled = true;

        public void setEnabled(boolean b) {
            enabled = b;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Object getValue(String key) {
            return null;
        }

        public void putValue(String key, Object value) {
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
