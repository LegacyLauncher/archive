package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;

import javax.swing.text.Document;

public class EditorTextField extends BorderPanel implements EditorField {
    private static final long serialVersionUID = 3920711425159165958L;
    private final boolean canBeEmpty;

    public final LocalizableTextField textField;

    public EditorTextField(String prompt, boolean canBeEmpty) {
        this.canBeEmpty = canBeEmpty;

        textField = new LocalizableTextField(prompt) {
            @Override
            protected void updateStyle() {
            }
        };
        textField.setColumns(1);
        setCenter(textField);
    }

    public EditorTextField(String prompt) {
        this(prompt, false);
    }

    public EditorTextField(boolean canBeEmpty) {
        this(null, canBeEmpty);
    }

    public EditorTextField() {
        this(false);
    }

    public String getSettingsValue() {
        return textField.getValue();
    }

    public void setSettingsValue(String value) {
        textField.setText(value);
        textField.setCaretPosition(0);
    }

    public boolean isValueValid() {
        String text = textField.getValue();
        return text != null || canBeEmpty;
    }

    public Document getDocument() {
        return textField.getDocument();
    }

    public void block(Object reason) {
        textField.setEnabled(false);
    }

    public void unblock(Object reason) {
        textField.setEnabled(true);
    }
}
