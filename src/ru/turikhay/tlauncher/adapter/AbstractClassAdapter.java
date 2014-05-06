package ru.turikhay.tlauncher.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.converter.dummy.DummyConverter;
import ru.turikhay.util.U;

@SuppressWarnings("unchecked")
public abstract class AbstractClassAdapter<T extends PublicCloneable> {
	
	protected final List<AdaptedValue<T>> values;
	protected final List<StringConverter<Object>> converters;
	
	protected T initial;
	
	protected AbstractClassAdapter() {
		this.values = new ArrayList<AdaptedValue<T>>();
		this.converters = new ArrayList<StringConverter<Object>>();
	}
	
	public T getInitial() {
		return initial;
	}
	
	public void setInitial(T instance) {
		this.initial = instance;
	}
	
	protected void addConverter(StringConverter<?> converter) {
		if(converter == null)
			throw new NullPointerException();
		
		this.converters.add((StringConverter<Object>) converter);
	}
	
	protected void addConverters(StringConverter<?>[] converters) {
		for(int i=0;i<converters.length;i++) {
			StringConverter<Object> converter = (StringConverter<Object>) converters[i];
			
			if(converter == null)
				throw new NullPointerException("StringConverter at "+i+" is NULL!");
			
			this.converters.add(converter);
		}
	}
	
	protected void removeConverter(StringConverter<?> converter) {
		if(converter == null)
			throw new NullPointerException();
		
		this.converters.remove(converter);		
	}
	
	protected List<StringConverter<Object>> getConverters() {
		return converters;
	}
	
	public synchronized void refreshValues() throws ClassAdapterException {
		if(initial == null)
			throw new NullPointerException("Initial instance is not defined!");
		
		ArrayList<AdaptedValue<T>> list = new ArrayList<AdaptedValue<T>>();		
		Field[] fields = initial.getClass().getDeclaredFields();
		
		for(Field field : fields) {
			Class<?> fieldClass = field.getType();
			StringConverter<Object> converter = null;
			
			for(StringConverter<Object> checkConverter : converters) {
				if(checkConverter.getObjectClass().equals( fieldClass )) {
					log("Found converter for field:", field);
					converter = checkConverter;
					break;
				}
			}
			
			if(converter == null) {
				log("Cannot find coverter for field:", field, fieldClass);
				continue;
			}
			
			try { field.setAccessible(true); }
			catch(RuntimeException e) {
				throw new ClassAdapterException("Field cannot be set accessible: "+ field, e);
			}
			
			String fieldName = field.getName();
			Object fieldValue;
			
			try { fieldValue = field.get(initial); }
			catch(Exception e) {
				throw new ClassAdapterException("Cannot get field value: "+field, e);
			}
			
			list.add(new AdaptedValue<T>(initial, fieldName, fieldValue, converter));
			
			try { field.setAccessible(false); }
			catch(RuntimeException e) {
				log("Cannot set default accessibility for field:", field);
			}
		}
		
		values.clear();
		values.addAll(list);
	}
	
	public synchronized T createInstance() throws ClassAdapterException {
		if(initial == null)
			throw new NullPointerException("Initial instance is not defined!");
		
		T instance;
		try {
			instance = (T) initial.cloneSafely();
			
			if(instance == null)
				throw new NullPointerException("New instance is NULL!");
			
		} catch(Exception e) {
			throw new ClassAdapterException("Cannot create clone instance!", e);
		}
		
		Field[] fields = instance.getClass().getDeclaredFields();
		
		for(AdaptedValue<T> adapted : values) {
			String fieldName = adapted.getKey();
			Field field = null;
			
			for(Field checkField : fields)
				if(fieldName.equals( checkField.getName() )) {
					field = checkField;
					break;
				}
			
			if(field == null) {
				log("No value for field:", fieldName);
				continue;
			}
			
			try { field.setAccessible(true); }
			catch(RuntimeException e) {
				throw new ClassAdapterException("Field cannot be set accessible: "+ field, e);
			}
			
			Object fieldValue = adapted.getValue();
			
			log("Setting:", fieldName, fieldValue);
			
			try { field.set(instance, fieldValue); }
			catch(Exception e) {
				throw new ClassAdapterException("Field cannot be set: "+field+" = "+ fieldValue, e);
			}
			
			try { field.setAccessible(false); }
			catch(RuntimeException e) {
				log("Cannot set default accessibility for field:", field);
			}
		}
		
		return instance;
	}
	
	protected void addDummyConverters() {
		this.addConverters(DummyConverter.getConverters());
	}
	
	protected void log(Object...o) {
		U.log("["+ getClass().getSimpleName() +"]", o);
	}
}
