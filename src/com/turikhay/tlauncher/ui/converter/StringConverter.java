package com.turikhay.tlauncher.ui.converter;

public interface StringConverter<T> {
	public T fromString(String from);
	public String toString(T from);
	
	public String toValue(T from);
}
