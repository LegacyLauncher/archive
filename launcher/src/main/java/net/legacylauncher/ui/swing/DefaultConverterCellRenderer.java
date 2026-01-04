package net.legacylauncher.ui.swing;

import lombok.Getter;
import net.legacylauncher.ui.converter.StringConverter;

import javax.swing.*;
import java.awt.*;

@Getter
public class DefaultConverterCellRenderer<T> extends DefaultListCellRenderer implements ConverterCellRenderer<T> {
    private final StringConverter<T> converter;

    public DefaultConverterCellRenderer(StringConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        //noinspection unchecked
        renderer.setText(converter.toString((T) value));
        return renderer;
    }
}
