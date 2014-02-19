package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class LocalizableStringConverter<T> implements StringConverter<T> {
	private final String prefix;
	
	public LocalizableStringConverter(String prefix){
		this.prefix = prefix;
	}

	@Override
	public String toString(T from) {
		return Localizable.get(getPath(from));
	}
	
	public String getPath(T from){
		String prefix = getPrefix();
		
		if(prefix == null || prefix.isEmpty())
			return toPath(from);
		
		String path = toPath(from);
		return prefix + "." + path;
	}
	
	public String getPrefix(){
		return prefix;
	}
	
	public abstract String toPath(T from);
}
