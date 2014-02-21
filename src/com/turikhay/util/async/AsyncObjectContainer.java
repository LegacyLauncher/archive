package com.turikhay.util.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsyncObjectContainer<T> {
	private List<AsyncObject<T>> objects;
	private Map<AsyncObject<T>, T> values;
	
	private boolean executionLock;
	
	public AsyncObjectContainer(){
		this.objects = new ArrayList<AsyncObject<T>>();
		this.values = new HashMap<AsyncObject<T>, T>();
	}
	
	public AsyncObjectContainer(AsyncObject<T>...asyncObjects){
		this();
		
		for(AsyncObject<T> object : asyncObjects)
			this.add(object);
	}
	
	public Map<AsyncObject<T>, T> execute() {
		this.executionLock = true;
		this.values.clear();
		
		synchronized(objects){
			int i = 0, size = objects.size();
			
			for(AsyncObject<T> object : objects)
				object.start();
			
			while(i < size){
				for(AsyncObject<T> object : objects)
					try {
						if(values.containsKey(object))
							continue;
						
						values.put(object, object.getValue());
						++i;
					} catch (AsyncObjectNotReadyException ignored) {
					}
					catch(AsyncObjectGotErrorException ignored0){
						values.put(object, null);
						++i;
					}
			}
		}
		
		this.executionLock = false;
		return values;
	}
	
	public void add(AsyncObject<T> object){
		if(object == null)
			throw new NullPointerException();
		
		synchronized(objects){
			if(executionLock)
				throw new AsyncContainerLockedException();
			
			this.objects.add(object);
		}
	}
	
	public void remove(AsyncObject<T> object){
		if(object == null)
			throw new NullPointerException();
		
		synchronized(objects){
			if(executionLock)
				throw new AsyncContainerLockedException();
			
			this.objects.remove(object);
		}
	}
	
	public void removeAll(){
		synchronized(objects){
			if(executionLock)
				throw new AsyncContainerLockedException();
			
			this.objects.clear();
		}
	}
}
