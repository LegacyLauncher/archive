package net.legacylauncher.ui.swing;

import net.legacylauncher.ui.converter.StringConverter;

import javax.swing.*;

public abstract class ConverterCellRenderer<T> implements ListCellRenderer<T> {
    protected final StringConverter<T> converter;

    protected ConverterCellRenderer(StringConverter<T> converter) {
        if (converter == null) {
            throw new NullPointerException();
        } else {
            this.converter = converter;
        }
    }

    public StringConverter<T> getConverter() {
        return converter;
    }
}
