package net.legacylauncher.ui.swing;

import lombok.Getter;
import net.legacylauncher.ui.converter.StringConverter;

import javax.swing.*;

@Getter
public abstract class ConverterCellRenderer<T> implements ListCellRenderer<T> {
    protected final StringConverter<T> converter;

    protected ConverterCellRenderer(StringConverter<T> converter) {
        if (converter == null) {
            throw new NullPointerException();
        }
        this.converter = converter;
    }
}
