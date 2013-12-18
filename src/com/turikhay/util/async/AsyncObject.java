package com.turikhay.util.async;

import com.turikhay.tlauncher.handlers.ExceptionHandler;

public abstract class AsyncObject<E> extends Thread {
	
	private boolean gotValue;
	private E value;
	private AsyncObjectGotErrorException error;

	public void run(){
		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
		
		try{
			this.value = this.execute();
		}catch(Throwable e){
			this.error = new AsyncObjectGotErrorException(this, error);
			return;
		}
		
		this.gotValue = true;
	}
	
	public E getValue() throws AsyncObjectNotReadyException, AsyncObjectGotErrorException {
		if(error != null)
			throw error;
		if(!gotValue)
			throw new AsyncObjectNotReadyException();
		
		return value;
	}
	
	public AsyncObjectGotErrorException getError(){
		return error;
	}
	
	protected abstract E execute();
}
