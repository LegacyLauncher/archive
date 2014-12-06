package ru.turikhay.util.async;

import java.util.concurrent.atomic.AtomicInteger;

import ru.turikhay.util.U;

public abstract class ExtendedThread extends Thread {
	private static AtomicInteger threadNum = new AtomicInteger();

	private final ExtendedThreadCaller caller;
	private String blockReason;

	public ExtendedThread(String name) {
		super(name + "#" + (threadNum.incrementAndGet()));

		this.caller = new ExtendedThreadCaller();
	}

	public ExtendedThread() {
		this("ExtendedThread");
	}

	public ExtendedThreadCaller getCaller() {
		return caller;
	}

	/**
	 * Starts new thread with <code>start()</code> method and waits until it is
	 * blocked.
	 */
	public void startAndWait() {
		super.start();

		while (!isThreadLocked())
			U.sleepFor(100);
	}

	@Override
	public abstract void run();

	private final Object monitor = new Object();

	protected void lockThread(String reason) {
		if (reason == null)
			throw new NullPointerException();

		checkCurrent();

		this.blockReason = reason;

		//threadLog("Thread locked by:", blockReason);

		synchronized(monitor) {
			while (blockReason != null)
				try {
					monitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		//threadLog("Thread has been unlocked");
	}

	public void unlockThread(String reason) {
		if (reason == null)
			throw new NullPointerException();

		//threadLog("Trying to unlock thread:", reason, "from", Thread.currentThread());

		if(!reason.equals(blockReason))
			throw new IllegalStateException("Unlocking denied! Locked with: "+ blockReason + ", tried to unlock with: "+ reason);

		this.blockReason = null;

		synchronized(monitor) {
			monitor.notifyAll();
		}

		//threadLog("Unlocked from", Thread.currentThread());
	}

	public void tryUnlock(String reason) {
		if(reason == null)
			throw new NullPointerException();

		if(reason.equals(blockReason))
			unlockThread(reason);
	}

	public boolean isThreadLocked() {
		return blockReason != null;
	}

	public boolean isCurrent() {
		return Thread.currentThread().equals(this);
	}

	protected void checkCurrent() {
		if (!isCurrent())
			throw new IllegalStateException("Illegal thread!");
	}

	protected void threadLog(Object... o) {
		U.log("[" + getName() + "]", o);
	}

	public class ExtendedThreadCaller extends RuntimeException {
		private ExtendedThreadCaller(){}
	}
}
