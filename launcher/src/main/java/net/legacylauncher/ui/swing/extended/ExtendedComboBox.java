package net.legacylauncher.ui.swing.extended;

import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.converter.StringConverter;
import net.legacylauncher.ui.swing.DefaultConverterCellRenderer;
import net.legacylauncher.ui.swing.SimpleComboBoxModel;
import net.legacylauncher.ui.theme.Theme;

import javax.swing.*;

public class ExtendedComboBox<T> extends JComboBox<T> {
    private static final long serialVersionUID = -4509947341182373649L;
    private StringConverter<T> converter;

    public ExtendedComboBox(ListCellRenderer<T> renderer) {
        Theme.setup(this);
        setModel(new SimpleComboBoxModel<>());
        setRenderer(renderer);
        setOpaque(false);
        setFont(getFont().deriveFont(LegacyLauncherFrame.getFontSize()));
        ((JComponent) getEditor().getEditorComponent()).setOpaque(false);
    }

    public ExtendedComboBox(StringConverter<T> converter) {
        this(new DefaultConverterCellRenderer<>(converter));
        this.converter = converter;
    }

    public ExtendedComboBox() {
        this((ListCellRenderer<T>) null);
    }

    public SimpleComboBoxModel<T> getSimpleModel() {
        return (SimpleComboBoxModel<T>) getModel();
    }

    @SuppressWarnings("unchecked")
    public T getSelectedValue() {
        return (T) getSelectedItem();
    }

    public void setSelectedValue(T value) {
        setSelectedItem(value);
    }

    public void setSelectedValue(String string) {
        T value = convert(string);
        if (value != null) {
            setSelectedValue(value);
        }
    }

    public StringConverter<T> getConverter() {
        return converter;
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }

    protected String convert(T obj) {
        return converter != null ? converter.toValue(obj) : (obj == null ? null : obj.toString());
    }

    protected T convert(String from) {
        return converter == null ? null : converter.fromString(from);
    }
}
