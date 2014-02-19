package com.turikhay.util.async;

public class ExtendedThread extends Thread {
	private static int threadNum;
	
	private final ExtendedThreadCaller caller;
	
    public ExtendedThread() {
        super("ExtendedThread#" + (threadNum++));
        
        this.caller = new ExtendedThreadCaller();
    }
    
    public ExtendedThread(String name) {
    	super(name);
    	
    	this.caller = new ExtendedThreadCaller();
    }
    
    public ExtendedThreadCaller getCaller(){
    	return caller;
    }
    
    public class ExtendedThreadCaller extends RuntimeException {
		private static final long serialVersionUID = -9184403765829112550L;
    }
}
