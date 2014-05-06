package ru.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ru.turikhay.tlauncher.ui.console.Console;

public class DownloadableContainer {
	private final List<DownloadableContainerHandler> handlers;
	private final List<Throwable> errors;
	final List<Downloadable> list;

	private Console console;

	private final AtomicInteger sum;
	private boolean locked, aborted;

	public DownloadableContainer() {
		this.list = Collections.synchronizedList(new ArrayList<Downloadable>());
		this.handlers = Collections
				.synchronizedList(new ArrayList<DownloadableContainerHandler>());
		this.errors = Collections.synchronizedList(new ArrayList<Throwable>());

		this.sum = new AtomicInteger();
	}

	public List<Downloadable> getList() {
		return Collections.unmodifiableList(list);
	}

	public void add(Downloadable d) {
		if (d == null)
			throw new NullPointerException();

		checkLocked();

		list.add(d);
		d.setContainer(this);

		sum.incrementAndGet();
	}

	public void addAll(Downloadable... ds) {
		if (ds == null)
			throw new NullPointerException();

		for (int i = 0; i < ds.length; i++) {
			if (ds[i] == null)
				throw new NullPointerException("Downloadable at " + i
						+ " is NULL!");

			list.add(ds[i]);
			ds[i].setContainer(this);

			sum.incrementAndGet();
		}
	}

	public void addAll(Collection<Downloadable> coll) {
		if (coll == null)
			throw new NullPointerException();

		int i = -1;

		for (Downloadable d : coll) {
			++i;

			if (d == null)
				throw new NullPointerException("Downloadable at" + i
						+ " is NULL!");

			list.add(d);
			d.setContainer(this);

			sum.incrementAndGet();
		}
	}

	public void addHandler(DownloadableContainerHandler handler) {
		if (handler == null)
			throw new NullPointerException();

		checkLocked();

		handlers.add(handler);
	}

	public List<Throwable> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	public Console getConsole() {
		return console;
	}

	public boolean hasConsole() {
		return console != null;
	}

	public void setConsole(Console console) {
		checkLocked();

		this.console = console;
	}

	public boolean isAborted() {
		return aborted;
	}

	void setLocked(boolean locked) {
		this.locked = locked;
	}

	void checkLocked() {
		if (locked)
			throw new IllegalStateException("Downloadable is locked!");
	}

	void onStart() {
		for (DownloadableContainerHandler handler : handlers)
			handler.onStart(this);
	}

	void onComplete(Downloadable d) throws RetryDownloadException {
		for (DownloadableContainerHandler handler : handlers)
			handler.onComplete(this, d);

		if (sum.decrementAndGet() > 0)
			return;

		for (DownloadableContainerHandler handler : handlers)
			handler.onFullComplete(this);
	}

	void onAbort(Downloadable d) {
		aborted = true;
		errors.add(d.getError());

		if (sum.decrementAndGet() > 0)
			return;

		for (DownloadableContainerHandler handler : handlers)
			handler.onAbort(this);
	}

	void onError(Downloadable d, Throwable e) {
		errors.add(e);

		for (DownloadableContainerHandler handler : handlers)
			handler.onError(this, d, e);
	}

	void log(Object... o) {
		if (console == null)
			return;
		console.log(o);
	}
	
	public static void removeDublicates(DownloadableContainer a, DownloadableContainer b) {
		if(a.locked)
			throw new IllegalStateException("First conatiner is already locked!");
		
		if(b.locked)
			throw new IllegalStateException("Second container is already locked!");
		
		a.locked = true;
		b.locked = true;
		
		try {
			List<Downloadable> aList = a.list, bList = b.list, deleteList = new ArrayList<Downloadable>();
		
			for(Downloadable aDownloadable : aList)
			for(Downloadable bDownloadable : bList)
				if(aDownloadable.equals(bDownloadable))
					deleteList.add(bDownloadable);
		
			bList.removeAll(deleteList);
			
		} finally {
			a.locked = false;
			b.locked = false;
		}
	}
	
	public static void removeDublicates(List<? extends DownloadableContainer> list) {
		if(list == null)
			throw new NullPointerException();
		
		if(list.size() < 2)
			return;
		
		for(int i=0;i<list.size()-1;i++)
		for(int k=i+1;k<list.size();k++)
			DownloadableContainer.removeDublicates(list.get(i), list.get(k));
	}
}
