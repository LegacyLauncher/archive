package ru.turikhay.util;

import java.lang.reflect.Field;

import ru.turikhay.exceptions.ParseException;

/**
 * Safely use Java Reflection methods. Never throw anything, just return <code>null</code>.
 * @author turikhay
 */
public class Reflect {

	public static Field getField0(Class<?> clazz, String name) throws NoSuchFieldException, SecurityException {
		if(clazz == null)
			throw new NullPointerException("class is null");

		if(name == null || name.isEmpty())
			throw new NullPointerException("name is null or empty");

		return clazz.getField(name);
	}

	public static Field getField(Class<?> clazz, String name) {
		try { return getField0(clazz, name); }
		catch(Exception e) {
			U.log("Error getting field", name, "from", clazz, e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getValue0(Field field, Class<T> classOfT, Object parent) throws IllegalArgumentException, IllegalAccessException {
		if(field == null)
			throw new NullPointerException("field is null");

		if(classOfT == null)
			throw new NullPointerException("classOfT is null");

		if(parent == null)
			throw new NullPointerException("parent is NULL");

		Class<?> fieldClass = field.getType();

		if(fieldClass == null)
			// I don't think that this ever happen, but... nevermind.
			throw new NullPointerException("field has no shell");

		if(!fieldClass.equals(classOfT) && !fieldClass.isAssignableFrom(classOfT)) // De Morgan, fuck off
			throw new IllegalArgumentException("field is not assignable from return type class");

		return (T) field.get(parent);
	}

	public static <T> T getValue(Field field, Class<T> classOfT, Object parent) {
		try { return getValue0(field, classOfT, parent); }
		catch(Exception e) {
			U.log("Cannot get value of", field, "from", classOfT, parent, e);
		}
		return null;
	}

	public static <T> T cast(Object o, Class<T> classOfT) {
		if (classOfT == null)
			throw new NullPointerException();

		return classOfT.isInstance(o) ? classOfT.cast(o) : null;
	}

	public static <T extends Enum<T>> T parseEnum0(Class<T> enumClass, String string) throws ParseException {
		if(enumClass == null)
			throw new NullPointerException("class is null");

		if(string == null)
			throw new NullPointerException("string is null");

		T[] constants = enumClass.getEnumConstants();
		for(T constant : constants)
			if(string.equalsIgnoreCase( constant.toString() ))
				return constant;

		throw new ParseException("Cannot parse value:\""+ string+"\"; enum: "+ enumClass.getSimpleName());
	}

	public static <T extends Enum<T>> T parseEnum(Class<T> enumClass, String string) {
		try {
			return parseEnum0(enumClass, string);
		}catch(Exception e) {
			U.log(e);
		}
		return null;
	}

	private Reflect() {
		throw new RuntimeException();
	}
}
