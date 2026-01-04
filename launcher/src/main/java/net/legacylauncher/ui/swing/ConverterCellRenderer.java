package net.legacylauncher.ui.swing;

import net.legacylauncher.ui.converter.StringConverter;

public interface ConverterCellRenderer<T> {
    StringConverter<T> getConverter();
}
