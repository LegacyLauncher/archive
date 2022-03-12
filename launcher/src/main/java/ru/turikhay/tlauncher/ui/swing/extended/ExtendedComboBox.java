package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.swing.DefaultConverterCellRenderer;
import ru.turikhay.tlauncher.ui.swing.SimpleComboBoxModel;
import ru.turikhay.tlauncher.ui.theme.Theme;

import javax.swing.*;

public class ExtendedComboBox<T> extends JComboBox<T> {
    private static final long serialVersionUID = -4509947341182373649L;
    private StringConverter<T> converter;

    public ExtendedComboBox(ListCellRenderer<T> renderer) {
        Theme.setup(this);
        setModel(new SimpleComboBoxModel<>());
        setRenderer(renderer);
        setOpaque(false);
        setFont(getFont().deriveFont(TLauncherFrame.getFontSize()));
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
