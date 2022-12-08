package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.swing.ConverterCellRenderer;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

import javax.swing.*;

public class EditorComboBox<T> extends ExtendedComboBox<T> implements EditorField {
    private static final long serialVersionUID = -2320340434786516374L;
    private final boolean allowNull;

    public EditorComboBox(ConverterCellRenderer<T> renderer, boolean allowNull) {
        super(renderer);
        setConverter(renderer.getConverter());
        this.allowNull = allowNull;
    }

    public EditorComboBox(StringConverter<T> converter, T[] values, boolean allowNull) {
        super(converter);
        this.allowNull = allowNull;
        if (values != null) {
            for (T value : values) {
                addItem(value);
            }
        }
    }

    public EditorComboBox(StringConverter<T> converter, T[] values) {
        this(converter, values, false);
    }

    public String getSettingsValue() {
        T value = getSelectedValue();
        return convert(value);
    }

    public void setSettingsValue(String string) {
        T value = convert(string);
        if (!allowNull && string == null) {
            boolean hasNull = false;

            for (int i = 0; i < getItemCount(); ++i) {
                if (getItemAt(i) == null) {
                    hasNull = true;
                }
            }

            if (!hasNull) {
                return;
            }
        }

        setSelectedValue(value);
    }

    public boolean isValueValid() {
        return true;
    }

    public void block(Object reason) {
        setEnabled(false);
    }

    public void unblock(Object reason) {
        setEnabled(true);
    }
}
