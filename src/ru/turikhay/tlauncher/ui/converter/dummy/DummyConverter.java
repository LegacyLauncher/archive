package ru.turikhay.tlauncher.ui.converter.dummy;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class DummyConverter<T> implements StringConverter<T> {
	private static DummyConverter<Object>[] converters;
	
	@SuppressWarnings("unchecked")
	public static DummyConverter<Object>[] getConverters() {
		if(converters == null)		
			converters = new DummyConverter[]{
				new DummyStringConverter(),
				new DummyIntegerConverter(),
				new DummyDoubleConverter(),
				new DummyLongConverter(),
				new DummyDateConverter()
			};
		return converters;
	}

	@Override
	public T fromString(String from) {
		return fromDummyString(from);
	}

	@Override
	public String toString(T from) {
		return toValue(from);
	}

	@Override
	public String toValue(T from) {
		return toDummyValue(from);
	}
	
	public abstract T fromDummyString(String from) throws RuntimeException;
	public abstract String toDummyValue(T value) throws RuntimeException;
}
