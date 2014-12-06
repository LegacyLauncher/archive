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
	protected final void lockThread(String reason) {
		if (reason == null)
			throw new NullPointerException();

		if(!reason.equals(LOOPED_BLOCK))
			throw new IllegalArgumentException("Illegal block reason. Expected: "+ LOOPED_BLOCK +", got: "+ reason);

		super.lockThread(reason);
	}

	public final boolean isIterating() {
		return !isThreadLocked();
	}

	public final void iterate() {
		if(!isIterating())
			unlockThread(LOOPED_BLOCK);
	}

	@Override
	public final void run() {
		while(true) {
			lockThread(LOOPED_BLOCK);
			iterateOnce();
		}
	}

	protected abstract void iterateOnce();

}
