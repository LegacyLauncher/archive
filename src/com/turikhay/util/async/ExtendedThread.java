package com.turikhay.util.async;

import com.turikhay.util.U;

public abstract class ExtendedThread extends Thread {
	private static int threadNum;

	private final ExtendedThreadCaller caller;
	private String blockReason;

	public ExtendedThread(String name) {
		super(name + "#" + (threadNum++));

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

		while (!isThreadBlocked())
			U.sleepFor(100);
	}

	@Override
	public abstract void run();

	protected synchronized void blockThread(String reason) {
		if (reason == null)
			throw new NullPointerException();

		checkCurrent();

		this.blockReason = reason;

		threadLog("Thread locked by:", blockReason);

		while (blockReason != null)
			try {
				wait();
			} catch (InterruptedException interrputed) {
				return;
			}

		threadLog("Thread has been unlocked");
	}

	public synchronized void unblockThread(String reason) {
		if (reason == null)
			throw new NullPointerException();

		if (!reason.equals(blockReason))
			throw new IllegalStateException("Unlocking denied! Locked with: "
					+ blockReason + ", tried to unlock with: " + reason);

		this.blockReason = null;
		notifyAll();
	}

	public boolean isThreadBlocked() {
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
		private static final long serialVersionUID = -9184403765829112550L;
		
		private ExtendedThreadCaller(){}
	}
}
