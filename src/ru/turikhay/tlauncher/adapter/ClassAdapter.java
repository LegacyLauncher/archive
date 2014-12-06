package ru.turikhay.tlauncher.adapter;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public class ClassAdapter<T extends PublicCloneable> extends AbstractClassAdapter<T> {
	
	public ClassAdapter() {
		super();
		addDummyConverters();
	}
	
	@Override
	public void addConverter(StringConverter<?> converter) {
		super.addConverter(converter);
	}
}
