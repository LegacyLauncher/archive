package ru.turikhay.tlauncher.ui.swing;

import javax.swing.ListCellRenderer;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class ConverterCellRenderer<T> implements ListCellRenderer<T> {
	protected final StringConverter<T> converter;

	ConverterCellRenderer(StringConverter<T> converter) {
		if (converter == null)
			throw new NullPointerException();

		this.converter = converter;
	}

	public StringConverter<T> getConverter() {
		return converter;
	}

}
