package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import com.turikhay.tlauncher.ui.converter.StringConverter;

public class DefaultConverterCellRenderer<T> extends ConverterCellRenderer<T> {
	protected final DefaultListCellRenderer defaultRenderer;

	public DefaultConverterCellRenderer(StringConverter<T> converter) {
		super(converter);
		
		this.defaultRenderer = new DefaultListCellRenderer();
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends T> list, T value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
		        isSelected, cellHasFocus);
		
		renderer.setText(converter.toString(value));
		
		return renderer;
	}

}
