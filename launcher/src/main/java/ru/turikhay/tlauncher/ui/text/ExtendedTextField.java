package ru.turikhay.tlauncher.ui.text;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ExtendedTextField extends JTextField {
    private static final long serialVersionUID = -1963422246993419362L;
    private CenterPanelTheme theme;
    private String placeholder;
    private String oldPlaceholder;

    protected ExtendedTextField(CenterPanel panel, String placeholder, String value) {
        theme = panel == null ? CenterPanel.defaultTheme : panel.getTheme();
        this.placeholder = placeholder;
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                onFocusGained();
            }

            public void focusLost(FocusEvent e) {
                onFocusLost();
            }
        });
        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                onChange();
            }

            public void removeUpdate(DocumentEvent e) {
                onChange();
            }

            public void changedUpdate(DocumentEvent e) {
                onChange();
            }
        });
        setValue(value);
        updateStyle();
    }

    public ExtendedTextField(String placeholder, String value) {
        this(null, placeholder, value);
    }

    public ExtendedTextField(String placeholder) {
        this(null, placeholder, null);
    }

    public ExtendedTextField() {
        this(null, null, null);
    }

    private String getValueOf(String value) {
        return value != null && !value.isEmpty() && !value.equals(placeholder) && !value.equals(oldPlaceholder) ? value : null;
    }

    public String getValue() {
        return getValueOf(getText());
    }

    public void setText(String text) {
        String value = getValueOf(text);
        if (value == null) {
            setPlaceholder();
        } else {
            setForeground(theme.getFocus());
            setRawText(value);
        }

    }

    private void setPlaceholder() {
        setForeground(theme.getFocusLost());
        setRawText(placeholder);
    }

    private void setEmpty() {
        setForeground(theme.getFocus());
        setRawText("");
    }

    protected void updateStyle() {
        setForeground(getValue() == null ? theme.getFocusLost() : theme.getFocus());
        setBackground(theme.getBackground());
    }

    public void setValue(Object obj) {
        setText(obj == null ? null : obj.toString());
    }

    protected void setValue(String s) {
        setText(s);
    }

    protected void setRawText(String s) {
        super.setText(s);
        super.setCaretPosition(0);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    protected void setPlaceholder(String placeholder) {
        oldPlaceholder = this.placeholder;
        this.placeholder = placeholder;
        if (getValue() == null) {
            setPlaceholder();
        }

    }

    public CenterPanelTheme getTheme() {
        return theme;
    }

    protected void setTheme(CenterPanelTheme theme) {
        if (theme == null) {
            theme = CenterPanel.defaultTheme;
        }

        this.theme = theme;
        updateStyle();
    }

    public void onFocusGained() {
        if (getValue() == null) {
            setEmpty();
        }
    }

    protected void onFocusLost() {
        if (getValue() == null) {
            setPlaceholder();
        }

    }

    protected void onChange() {
    }
}
