package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.managers.ComponentManager;

/**
 * An abstract <code>RefreshableComponent</code> whose refresh process can be
 * cancelled.
 * 
 * @author Artur Khusainov
 * 
 */
public abstract class InterruptibleComponent extends RefreshableComponent {
	protected final boolean[] refreshList;
	private int lastRefreshID;

	protected InterruptibleComponent(ComponentManager manager) throws Exception {
		this(manager, 64); // default size.
	}

	private InterruptibleComponent(ComponentManager manager, int listSize)
			throws Exception {
		super(manager);

		if (listSize < 1)
			throw new IllegalArgumentException("Invalid list size: " + listSize
					+ " < 1");

		this.refreshList = new boolean[listSize];
	}

	/**
	 * Starts a new refresh process with next queueID.
	 */
	public boolean startRefresh() {
		return refresh(nextID());
	}

	@Override
	protected boolean refresh() {
		return startRefresh();
	}

	/**
	 * Sets every refresh process as stopped.
	 */
	public synchronized void stopRefresh() {
		for (int i = 0; i < refreshList.length; i++)
			refreshList[i] = false;
	}

	protected synchronized int nextID() {
		int listSize = refreshList.length, next = lastRefreshID++;

		if (next >= listSize)
			next = 0;

		this.lastRefreshID = next;
		return next;
	}

	protected boolean isCancelled(int refreshID) {
		return !refreshList[refreshID];
	}

	/**
	 * Refreshes information and then flushes it if the <code>refreshID</code>
	 * is not set as stopped.
	 */
	protected abstract boolean refresh(int refreshID);
}
