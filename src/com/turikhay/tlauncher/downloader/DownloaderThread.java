package com.turikhay.tlauncher.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.turikhay.tlauncher.util.U;

public class DownloaderThread extends Thread {
	public final String name;
	public final int id, maxAttempts = 10;
	
	private final Downloader fd;
	private boolean launched, available = true, list_ = true;
	
	private List<Downloadable> list = new ArrayList<Downloadable>();
	private List<Downloadable> queue = new ArrayList<Downloadable>();
	
	private int done, remain, progress;
	private double each, av_speed;
	private Throwable error;
	
	public DownloaderThread(Downloader td, int tid){ fd = td; name = fd.name; id = tid; this.start(); }
	
	public void run(){ check();
		while(!launched) sleepFor(500);		
		available = false;
		
		done = progress = 0;
		int all = list.size(); remain = all; each = 100.0 / all;
		
		log("Received "+all+" files");
		
		for(Downloadable d : list){ error = null;			
			onStart(d);
			
			int attempt = 0;
			
			while(attempt < maxAttempts){
				++attempt;
				log("Attempting to download "+ d.getURL() + " ["+attempt+"/"+maxAttempts+"]...");
				
				try{
					download(d);
					log("Downloaded! ["+attempt+"/"+maxAttempts+"]");
					break;
				}catch(DownloaderError de){
					if(!de.isSerious()){
						if(de.hasTimeout()) sleepFor(de.getTimeout());
						continue;
					}
					de.printStackTrace();
					onError(d, de); break;
				}catch(SocketTimeoutException se){
					log("Timeout exception. Retrying.");
					sleepFor(5000);
					continue;
				}catch(Exception e){
					log("Unknown error occurred.");
					e.printStackTrace();
					onError(d, e); break;
				}
			}
		}
		
		while(!list_) sleepFor(100);
		list_ = false;
		
		list.clear();		
		list.addAll(queue);
		queue.clear();
		
		list_ = true;
		
		available = true;
		launched = list.size() > 0;
		run();
	}
	
	public void download(Downloadable d) throws IOException {
		String fn = d.getFilename();
		
		long reply_s = System.currentTimeMillis();
		HttpURLConnection connection = d.makeConnection();
		long reply = System.currentTimeMillis() - reply_s;
		log("Got reply in " + reply + " ms.");
		
		int code = connection.getResponseCode();
		switch(code){
		case 301:
		case 302:
		case 303:
		case 307:
			String newurl = connection.getHeaderField("Location"); connection.disconnect();
			if(newurl == null)
				throw new DownloaderError("Redirection is required but field \"Location\" is empty", true);
			
			log("Responce code is "+code+". Redirecting to: "+newurl);			
			d.setURL(newurl); download(d); return;
		case 304:
			if(d.isForced()) break;			
			log("File is not modified (304)");
			onComplete(d); return;
		case 403:
			throw new DownloaderError("Forbidden (403)", true);
		case 404:
			throw new DownloaderError("Not Found (404)", true);
		case 408:
			throw new DownloaderError("Request Timeout (408)", false);
		case 500:
			throw new DownloaderError("Internal Server Error (500)", 5000);
		case 502:
			throw new DownloaderError("Bad Gateway (502)", 5000);
		case 503:
			throw new DownloaderError("Service Unavailable (503)", 5000);
		case 504:
			throw new DownloaderError("Gateway Timeout (504)", true);
		}
		if(code < 200 || code > 299) throw new DownloaderError("Illegal response code ("+code+")", true);
		
		File file = d.getDestination();
		if(!file.exists()){ file.getParentFile().mkdirs(); file.createNewFile(); }
		
		InputStream in = connection.getInputStream();
		OutputStream out = new FileOutputStream(file);
		
		long read = 0L, length = connection.getContentLengthLong(), downloaded_s = System.currentTimeMillis();
		byte[] buffer = new byte[65536]; // 16 kb
		
		int curread = in.read(buffer);
		while(curread > 0){ read += curread;
			out.write(buffer, 0, curread);
			
			long elapsed_s = System.nanoTime();
			curread = in.read(buffer);
			long curelapsed = System.nanoTime() - elapsed_s;
			
			double curdone = (read / (float) length);
			
			onProgress(curread, curelapsed, curdone);
		}
		
		long downloaded = System.currentTimeMillis() - downloaded_s;
		
		in.close();
		out.close();
		connection.disconnect();
		
		log("Successfully downloaded "+fn+" in "+(downloaded / 1000)+" s!");
		onComplete(d);
	}
	
	public void add(Downloadable d){
		while(!list_) sleepFor(100);
		
		list_ = false;
		if(available) list.add(d); else queue.add(d);
		list_ = true;
	}
	
	public boolean isAvailable(){ return available; }
	public boolean isLaunched(){ return launched; }
	
	public Throwable getError(){ return error; }
	public double getSpeed(){ return av_speed; }
	public int getProgress(){ return progress; }
	public int getRemain(){ return remain; }
	public int getDone(){ return done; }
	
	public void launch(){ launched = true; }
	
	// Events
	private void onStart(Downloadable d){
		d.onStart();
		if(d.hasContainter()) d.getContainer().onStart();
		
		fd.onStart(id, d);
	}
	private void onComplete(Downloadable d){
		d.onComplete();
		if(d.hasContainter()) d.getContainer().onFileComplete();
		
		--remain; ++done;
		progress = (int) (done * each);
		
		fd.onProgress(id, progress);
		fd.onComplete(id, d);
	}
	private void onError(Downloadable d, Throwable err){
		error = err; d.setError(err); d.onError();
		if(d.hasContainter()) d.getContainer().onError();
		fd.onError(id, d);
	}
	private void onProgress(int curread, long curelapsed, double curdone){
		int old_progress = progress;
		progress = (int) ((done * each) + (curdone * each));		
		if(progress == old_progress) return;
		
		fd.onProgress(id, progress);
	}
	
	// Util
	private void log(Object message){ U.log("["+name+"DT #"+id+"] ", message); }
	private void check(){
		if(available) return;
		throw new IllegalStateException("DownloaderThread (#"+id+") is unavailable!");
	}
	private void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){}
	}
}
