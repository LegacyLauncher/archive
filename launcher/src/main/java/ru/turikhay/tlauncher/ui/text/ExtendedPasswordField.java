package ru.turikhay.tlauncher.ui.text;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class ExtendedPasswordField extends JPasswordField {
    private static final long serialVersionUID = 3175896797135831502L;
    private static final String DEFAULT_PLACEHOLDER = "пассворд, лол";
    private CenterPanelTheme theme;
    private String placeholder;

    private ExtendedPasswordField(CenterPanel panel, String placeholder) {
        theme = panel == null ? CenterPanel.defaultTheme : panel.getTheme();
        this.placeholder = placeholder == null ? DEFAULT_PLACEHOLDER : placeholder;
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
        setText(null);
        updateStyle();
    }

    public ExtendedPasswordField() {
        this(null, null);
    }

    private String getValueOf(String value) {
        return value != null && !value.isEmpty() && !value.equals(placeholder) ? value : null;
    }

    @Override
    public char[] getPassword() {
        String value = getValue();
        return value == null ? new char[0] : value.toCharArray();
    }

    public boolean hasPassword() {
        return getValue() != null;
    }

    private String getValue() {
        return getValueOf(getText());
    }

    @Override
    public void setText(String text) {
        String value = getValueOf(text);
        if (value == null) {
            setPlaceholder();
        } else {
            setForeground(theme.getFocus());
            super.setText(value);
        }

    }

    private void setPlaceholder() {
        setForeground(theme.getFocusLost());
        super.setText(placeholder);
    }

    private void setEmpty() {
        setForeground(theme.getFocus());
        super.setText("");
    }

    void updateStyle() {
        setForeground(getValue() == null ? theme.getFocusLost() : theme.getFocus());
        setBackground(theme.getBackground());
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "пассворд, лол" : placeholder;
        if (getValue() == null) {
            setPlaceholder();
        }

    }

    public CenterPanelTheme getTheme() {
        return theme;
    }

    public void setTheme(CenterPanelTheme theme) {
        if (theme == null) {
            theme = CenterPanel.defaultTheme;
        }

        this.theme = theme;
        updateStyle();
    }

    protected void onFocusGained() {
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
