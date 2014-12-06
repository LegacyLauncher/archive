package com.turikhay.tlauncher.configuration;

import java.io.IOException;

/**
 * Abstract Configuration interface
 * 
 * @author Artur Khusainov
 * 
 */
public interface AbstractConfiguration {
	public String get(String key);

	public int getInteger(String key);

	public double getDouble(String key);

	public float getFloat(String key);

	public long getLong(String key);

	public boolean getBoolean(String key);

	public String getDefault(String key);

	public int getDefaultInteger(String key);

	public double getDefaultDouble(String key);

	public float getDefaultFloat(String key);

	public long getDefaultLong(String key);

	public boolean getDefaultBoolean(String key);

	public void set(String key, Object value);

	public void clear();

	public void save() throws IOException;
}
