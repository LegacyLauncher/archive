package com.turikhay.tlauncher.downloader;

import java.util.ArrayList;
import java.util.List;

import com.turikhay.tlauncher.util.U;

public class Downloader extends Thread {
	public final int maxThreads = 10;
	
	private boolean launched, available = true, list_ = true, listeners_ = true;
	
	private DownloaderThread[] threads = new DownloaderThread[ maxThreads ];
	
	private List<Downloadable> list = new ArrayList<Downloadable>();
	private List<Downloadable> queue = new ArrayList<Downloadable>();
	
	private List<DownloadListener> listeners = new ArrayList<DownloadListener>();
	
	private boolean[] running = new boolean[ maxThreads ];
	private int[] remain, progress;
	
	private int av_progress, threadsStarted;
	
	public Downloader(){ this.start(); }
	
	public void run(){ check();
		while(!launched) sleepFor(500);
		available = false;
		
		av_progress = 0;
		
		remain = new int[ maxThreads ];
		progress = new int[ maxThreads ];
		
		int len = list.size(), each = U.getMaxMultiply(len, maxThreads), x = 0, y = -1;
		
		if(len > 0) onDownloaderStart(len);
		
		while(len > 0){		
			for(int i=0;i<maxThreads;i++){ ++y;
				len -= each;
				remain[i] += each;
				
				DownloaderThread curthread = threads[i];
				if(curthread == null){ running[i] = ((curthread = threads[i] = new DownloaderThread(this, i)) != null); /* LOL :D */ ++threadsStarted; }
				for(y=x;y<x+each;y++) curthread.add(list.get(y));
				x = y;
				
				if(len == 0) break;
			}
			each = U.getMaxMultiply(len, maxThreads);
		}
		
		for(int i=0;i<maxThreads;i++)
			if(running[i]) threads[i].launch();
		
		while(!list_) sleepFor(100);
		list_ = false;
		
		list.clear();
		
		list_ = true;
		launched = false;
		while(!launched) sleepFor(500);
		
		while(!list_) sleepFor(100);
		list_= false;
		
		list.addAll(queue);
		queue.clear();
		
		list_ = true;
		
		available = true;
		run();
	}
	
	public void add(Downloadable d){
		while(!list_) sleepFor(100);
		
		list_ = false;
		if(available) list.add(d); else queue.add(d);
		list_ = true;
	}
	public void add(DownloadableContainer c){
		while(!list_) sleepFor(100);
	
		list_ = false;
		if(available) list.addAll(c.elems); else queue.addAll(c.elems);
		list_ = true;
	}
	public void addListener(DownloadListener l){
		while(!listeners_) sleepFor(100);
		
		listeners_ = false;
		listeners.add(l);
		listeners_ = true;
	}
	public void removeListener(DownloadListener l){
		while(!listeners_) sleepFor(100);
		
		listeners_ = false;
		listeners.remove(l);
		listeners_ = true;
	}
	public boolean isAvailable(){ return available; }
	public boolean isLaunched(){ return launched; }
	
	public int getRemaining(){ return U.getSum(remain); }
	
	public void launch(){ launched = true; }
	
	void onDownloaderStart(int files){
		for(DownloadListener l : listeners)
			l.onDownloaderStart(this, files);
	}
	void onDownloaderStop(){
		for(DownloadListener l : listeners)
			l.onDownloaderComplete(this);
	}
	void onStart(int id, Downloadable d) {
		
	}
	void onError(int id, Downloadable d) { --remain[id];
		Throwable error = threads[id].getError();
		
		for(DownloadListener l : listeners)
			l.onDownloaderError(this, d, error);
	}
	void onProgress(int id, int curprogress) {
		progress[id] = curprogress;
		
		int old_progress = av_progress;
		av_progress = U.getAverage(progress, threadsStarted); if(av_progress == old_progress) return;
		
		for(DownloadListener l : listeners)
			l.onDownloaderProgress(this, av_progress);
	}
	void onComplete(int id, Downloadable d) { --remain[id];
		for(DownloadListener l : listeners)
			l.onDownloaderFileComplete(this, d);
	
		for(int curremain : remain) if(curremain != 0) return;
		
		for(DownloadListener l : listeners)
			l.onDownloaderComplete(this);
	}
	
	private void check(){
		if(available) return;
		throw new IllegalStateException("Downloader is unavailable!");
	}
	
	private void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){}
	}
}
