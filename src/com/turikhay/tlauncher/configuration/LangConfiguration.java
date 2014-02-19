package com.turikhay.tlauncher.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public class LangConfiguration extends SimpleConfiguration {
	private final Locale[] locales;
	private final Properties[] prop;
	
	private int i; // selected locale
	
	public LangConfiguration(Locale[] locales, Locale select) throws IOException {
		if(locales == null)
			throw new NullPointerException();
		
		int size = locales.length;
		
		this.locales = locales;
		this.prop = new Properties[size];
		
		for(int i=0;i<size;i++){
			Locale locale = locales[i];
			
			if(locale == null)
				throw new NullPointerException("Locale at #"+i+" is NULL!");
			
			String localeName = locale.toString();
			InputStream stream = getClass().getResourceAsStream("/lang/" + localeName + ".ini");
			
			if(stream == null)
				throw new IOException("Cannot find locale file for locale: " + localeName);
			
			prop[i] = loadFromStream(stream);
			
			if(localeName.equals("en_US"))
				copyProperties(prop[i], properties, true);
		}
		
		this.setSelected(select);
	}
	
	public Locale[] getLocales(){
		return locales;
	}
	
	public Locale getSelected(){
		return locales[i];
	}
	
	public void setSelected(Locale select){
		if(select == null)
			throw new NullPointerException();
		
		for(int i=0;i<locales.length;i++)
			if(locales[i].equals(select)){
				this.i = i;
				return;
			}
		
		throw new IllegalArgumentException("Cannot find Locale:" + select);
	}
	
	public String nget(String key) {
		if(key == null)
			return null;
		
		String value = prop[i].getProperty(key);
		
		if(value == null)
			return getDefault(key);
		
		return value;
	}
	
	@Override
	public String get(String key) {
		String value = nget(key);
		if(value == null)
			return key;
		
		return value;
	}
	
	public String nget(String key, Object...vars) {
		String value = get(key);
		
		if(value == null)
			return null;
		
		String[] variables = checkVariables(vars);
		
		for(int i=0;i<variables.length;i++)
			value = value.replace("%" + i, variables[i]);
		
		return value;
	}
	
	public String get(String key, Object...vars) {
		String value = nget(key, vars);
		if(value == null)
			return key;
		
		return value;
	}
	
	@Override
	public void set(String key, Object value) {
		// Nope. Never.
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getDefault(String key) {
		return super.get(key);
	}
	
	public static String[] checkVariables(Object[] check){
		if(check == null)
			throw new NullPointerException();
		
		if(check.length == 1 && check[0] == null)
			return new String[0];
		
		String[] string = new String[check.length];
		
		for(int i=0;i<check.length;i++)
			if(check[i] == null)
				throw new NullPointerException("Variable at index "+i+" is NULL!");
			else
				string[i] = check[i].toString();
		
		return string;
	}
}
