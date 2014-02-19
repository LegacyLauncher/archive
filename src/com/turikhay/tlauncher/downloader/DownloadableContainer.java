package com.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.ui.console.Console;

public class DownloadableContainer {
	private List<DownloadableHandler> handlers = Collections.synchronizedList(new ArrayList<DownloadableHandler>());
	private Console console;
	private boolean available = true;
	private int remain, errors;
	
	List<Downloadable> elems = new ArrayList<Downloadable>();
	
	public DownloadableContainer(){}
	public void addAll(Downloadable[] c){ for(Downloadable d : c) add(d); }
	public void addAll(Collection<Downloadable> c){ for(Downloadable d : c) add(d); }
	public void add(Downloadable d){ check(); elems.add(d); d.setContainer(this); ++remain; }
	public void remove(Downloadable d){ check(); elems.remove(d); d.setContainer(this); ++remain; }
	
	public List<Downloadable> get(){ check();
		List<Downloadable> t = new ArrayList<Downloadable>();
		t.addAll(elems);
		return t;
	}
	
	public boolean isAvailable(){ return available; }
	
	public void addHandler(DownloadableHandler newhandler){ check(); handlers.add(newhandler); }
	public List<DownloadableHandler> getHandlers(){
		List<DownloadableHandler> toret = new ArrayList<DownloadableHandler>();
		for(DownloadableHandler h : handlers)
			toret.add(h);
		return toret;
	}
	public void setConsole(Console c){ console = c; }
	public Console getConsole(){ return console; }
	public boolean hasConsole(){ return console != null; }
	
	void log(Object... obj){ if(console != null) console.log(obj); }
	void onError(){ ++errors; onFileComplete(); }
	void onStart(){ for(DownloadableHandler h : handlers) h.onStart(); }
	void onAbort(){ for(DownloadableHandler h : handlers) h.onAbort(); }
	void onFileComplete(){ --remain;
		if(remain != 0) return;
		if(errors > 0) for(DownloadableHandler h : handlers) h.onCompleteError();
		else for(DownloadableHandler h : handlers) h.onComplete();
	}
	public int getErrors(){ return errors; }
	
	private void check(){
		if(available) return;
		throw new IllegalStateException("DownloadableContainer is unavailable!");
	}
}
