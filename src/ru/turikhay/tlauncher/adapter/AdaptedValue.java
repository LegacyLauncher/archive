package ru.turikhay.tlauncher.adapter;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public class AdaptedValue<T> {
	private final T object;
	
	private final String key;
	private Object value;
	
	private final StringConverter<Object> converter;
	
	public AdaptedValue(T object, String key, Object value, StringConverter<Object> converter) {
		if(object == null)
			throw new NullPointerException("Object is NULL!");
		
		if(key == null)
			throw new NullPointerException("Key is NULL!");
		
		if(converter == null)
			throw new NullPointerException("Converter is NULL!");
		
		this.object = object;
		this.key = key;
		this.value = value;
		
		this.converter = converter;
	}
	
	public T getObject() {
		return object;
	}
	
	public String getKey() {
		return key;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String getStringValue() {
		return converter.toValue(value);
	}
	
	public StringConverter<Object> getConverter() {
		return converter;
	}
}
