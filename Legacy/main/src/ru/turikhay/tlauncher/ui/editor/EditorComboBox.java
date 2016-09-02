package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class EditorComboBox<T> extends ExtendedComboBox<T> implements EditorField {
    private static final long serialVersionUID = -2320340434786516374L;
    private final boolean allowNull;

    public EditorComboBox(StringConverter<T> converter, T[] values, boolean allowNull) {
        super(converter);
        this.allowNull = allowNull;
        if (values != null) {
            T[] var7 = values;
            int var6 = values.length;

            for (int var5 = 0; var5 < var6; ++var5) {
                T value = var7[var5];
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