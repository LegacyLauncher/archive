package ru.turikhay.util.async;

public abstract class LoopedThread extends ExtendedThread {
	protected static final String LOOPED_BLOCK = "iteration";
	
	public LoopedThread(String name) {
		super(name);
	}

	public LoopedThread() {
		this("LoopedThread");
	}
	
	@Override
	protected final synchronized void blockThread(String reason) {
		if (reason == null)
			throw new NullPointerException();
		
		if(!reason.equals(LOOPED_BLOCK))
			throw new IllegalArgumentException("Illegal block reason. Expected: "+ LOOPED_BLOCK +", got: "+ reason);
		
		super.blockThread(reason);
	}
	
	public final boolean isIterating() {
		return !isThreadBlocked();
	}
	
	public final void iterate() {
		if(!isIterating())
			unblockThread(LOOPED_BLOCK);
	}

	@Override
	public final void run() {
		while(true) {
			blockThread(LOOPED_BLOCK);
			iterateOnce();
		}
	}
	
	protected abstract void iterateOnce();

}
