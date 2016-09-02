package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

import javax.swing.*;
import java.awt.*;

public class DefaultConverterCellRenderer<T> extends ConverterCellRenderer<T> {
    private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public DefaultConverterCellRenderer(StringConverter<T> converter) {
        super(converter);
    }

    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        renderer.setText(converter.toString(value));
        return renderer;
    }
}
