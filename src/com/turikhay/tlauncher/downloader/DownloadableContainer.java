package com.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DownloadableContainer {
	private DownloadableHandler handler;
	private boolean available = true;
	private int remain, errors;
	
	Downloadable error_elem;
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
	
	public void setHandler(DownloadableHandler newhandler){ check(); handler = newhandler; }
	
	void onError(){ ++errors; onFileComplete(); }
	void onStart(){ if(handler != null) handler.onStart(); }
	void onFileComplete(){ --remain;
		if(remain != 0 || handler == null) return;
		
		if(errors > 0) handler.onCompleteError();
		else handler.onComplete();
	}
	
	public Throwable getError(){ if(error_elem != null) return error_elem.getError(); return null; }
	public int getErrors(){ return errors; }
	
	private void check(){
		if(available) return;
		throw new IllegalStateException("DownloadableContainer is unavailable!");
	}
}
