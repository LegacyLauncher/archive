package ru.turikhay.tlauncher.ui.swing.combobox;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;
import java.util.function.Function;

class FilterEditor<T> extends BasicComboBoxEditor {
    private final IconTextComponent textField = new IconTextComponent();
    boolean editing;
    private final Function<T, IconText> displayTextFunction;
    private final Consumer<Boolean> editingChangeListener;
    private Object selected;
    private Caret caret;

    FilterEditor(Function<T, IconText> displayTextFunction,
                 Consumer<Boolean> editingChangeListener) {
        this.displayTextFunction = displayTextFunction;
        this.editingChangeListener = editingChangeListener;
        inhibitSelectingOnFocusGain();
    }

    public void updateState() {
        setItem(selected);
    }

    public void addChar() {
        if (!editing) {
            enableEditingMode();
        }
    }

    private void inhibitSelectingOnFocusGain() {
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    Document doc = textField.getDocument();
                    if (doc != null) {
                        textField.setCaretPosition(doc.getLength());
                    }
                });
            }
        });
    }

    private void enableEditingMode() {
        editing = true;
        textField.setText("");
        textField.setIcon(null);
        editingChangeListener.accept(true);
        restoreCaret();
    }

    void hideCaret() {
        caret = textField.getCaret();
        textField.setCaret(EMPTY_CARET);
    }

    void restoreCaret() {
        if (caret != null) {
            textField.setCaret(caret);
        }
    }

    public void reset() {
        if (editing) {
            editing = false;
            editingChangeListener.accept(false);
            hideCaret();
        }
    }

    @Override
    public Component getEditorComponent() {
        return textField;
    }

    public JTextField getTextField() {
        return textField;
    }

    @Override
    public void setItem(Object anObject) {
        if (!editing) {
            T t = (T) anObject;
            IconText text = displayTextFunction.apply(t);
            textField.setText(text.getText());
            textField.setIcon(text.getIcon());
        }
        this.selected = anObject;
    }

    @Override
    public Object getItem() {
        return selected;
    }

    public boolean isEditing() {
        return editing;
    }

    private final Caret EMPTY_CARET = new DefaultCaret() {
        @Override
        public void paint(Graphics g) {
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public boolean isSelectionVisible() {
            return false;
        }
    };
}