package ru.turikhay.tlauncher.ui.swing;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.text.ExtendedTextField;

import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

public class TextPopup extends MouseAdapter {
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
        } else {
            boolean isEditable = comp.isEditable();
            boolean isSelected = comp.getSelectedText() != null;
            boolean hasValue = comp instanceof ExtendedTextField ? ((ExtendedTextField) comp).getValue() != null : StringUtils.isNotEmpty(comp.getText());
            boolean pasteAvailable = isEditable && Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);
            JPopupMenu menu = new JPopupMenu();
            Action cut = isEditable && hasValue ? selectAction(comp, "cut-to-clipboard", "cut") : null;
            final Action copy = selectAction(comp, "copy-to-clipboard", "copy");
            Action paste = pasteAvailable ? selectAction(comp, "paste-from-clipboard", "paste", e1 -> {
                if (comp instanceof ExtendedTextField) {
                    ((ExtendedTextField) comp).onFocusGained();
                }
            }) : null;
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
                menu.add(cut).setText(Localizable.get("popup.cut"));
            }

            if (isSelected && copy != null) {
                menu.add(copy).setText(Localizable.get("popup.copy"));
            }

            if (paste != null) {
                menu.add(paste).setText(Localizable.get("popup.paste"));
            }

            if (selectAll != null) {
                if (menu.getComponentCount() > 0 && !(menu.getComponent(menu.getComponentCount() - 1) instanceof Separator)) {
                    menu.addSeparator();
                }

                menu.add(selectAll).setText(Localizable.get("popup.selectall"));
            }

            if (copyAll != null) {
                menu.add(copyAll).setText(Localizable.get("popup.copyall"));
            }

            if (menu.getComponentCount() == 0) {
                return null;
            } else {
                if (menu.getComponent(0) instanceof Separator) {
                    menu.remove(0);
                }

                if (menu.getComponent(menu.getComponentCount() - 1) instanceof Separator) {
                    menu.remove(menu.getComponentCount() - 1);
                }

                return menu;
            }
        }
    }

    private static Action selectAction(JTextComponent comp, String general, String fallback, final ActionListener listener) {
        Action action = comp.getActionMap().get(general);
        if (action == null) {
            action = comp.getActionMap().get(fallback);
        }
        if (action != null && listener != null) {
            final Action a = action;
            return new Action() {
                @Override
                public Object getValue(String key) {
                    return a.getValue(key);
                }

                @Override
                public void putValue(String key, Object value) {
                    a.putValue(key, value);
                }

                @Override
                public void setEnabled(boolean b) {
                    a.setEnabled(b);
                }

                @Override
                public boolean isEnabled() {
                    return a.isEnabled();
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener listener) {
                    a.addPropertyChangeListener(listener);
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener listener) {
                    a.removePropertyChangeListener(listener);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.actionPerformed(e);
                    a.actionPerformed(e);
                }
            };
        }
        return action;
    }

    private static Action selectAction(JTextComponent comp, String general, String fallback) {
        return selectAction(comp, general, fallback, null);
    }
}
