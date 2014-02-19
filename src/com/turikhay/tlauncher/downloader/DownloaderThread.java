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

import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public class DownloaderThread extends Thread {
	public final String name;
	public final int id;
	
	private final Downloader fd;
	private boolean launched, available = true, list_ = true;
	
	private List<Downloadable> list;
	private List<Downloadable> queue;
	
	private int done, remain, progress, av = 500, avi;
	private double each, speed;
	private double[] av_speed;
	private Throwable error;
	
	public DownloaderThread(Downloader td, int tid){		
		this.list = new ArrayList<Downloadable>();
		this.queue = new ArrayList<Downloadable>();
		
		this.av_speed = new double[av];
		
		fd = td; name = fd.name; id = tid;
		this.start();
	}
	
	public void run(){ check();
		while(!launched) sleepFor(500);		
		available = false;
		
		done = progress = 0;
		int all = list.size(); remain = all; each = 100.0 / all;
		
		for(Downloadable d : list){ error = null;		
			onStart(d);
			
			int attempt = 0, max = (d.getFast())? fd.getMinTries() : fd.getMaxTries();
			
			while(attempt < max){
				++attempt;
				dlog("Attempting to download "+ d.getURL() + " ["+attempt+"/"+max+"]...");
				
				try{
					download(d, attempt, max);
					break;
				}
				catch(DownloaderStoppedException ds){
					log(d, "Aborting...");
					
					if(d.getSize() > 0){
						log(d, "Nevermind, file has been downloaded in time C:");
						return; // Fully downloaded C:
					}
					
					if(!d.getDestination().delete()){
						log(d, "Cannot delete destination file, will be deleted on exit.");
						d.getDestination().deleteOnExit();
					} else log(d, "Successfully deleted destination file!");
					
					list.clear();
					queue.clear();
					
					available = true;
					launched = false;
					
					onAbort(d);			
					run();					
					break;
				}catch(DownloaderError de){
					if(!de.isSerious()){
						if(de.hasTimeout()) sleepFor(de.getTimeout());
						continue;
					}
					log(d, de);
					onError(d, de); break;
				}catch(SocketTimeoutException se){
					log(d, "Timeout exception. Retrying.");
					sleepFor(5000);
				}catch(Exception e){
					log(d, "Unknown error occurred.", e);
					onError(d, e); break;
				}
			}
			
			if(attempt < max) continue;
			log(d, "Gave up trying to download this file ["+attempt+"/"+max+"]");
			onError(d, new DownloaderError("Gave up trying to download this file", true));
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
	
	public void download(Downloadable d, int attempt, int max) throws IOException {
		File file = d.getDestination();
		
		try{ file.canWrite(); }
		catch(SecurityException se){
			throw new IOException("Cannot write into destination file!", se);
		}
		
		HttpURLConnection connection = d.makeConnection();
		
		if(!launched)
			throw new DownloaderStoppedException();
		
		int code = -1;
		long reply_s = System.currentTimeMillis();
		if(d.getFast()) connection.connect(); else code = connection.getResponseCode();
		long reply = System.currentTimeMillis() - reply_s;
		
		if(!launched)
			throw new DownloaderStoppedException();
		
		dlog("Got reply in " + reply + " ms. Response code:", code);
		
		switch(code){
		case -1: break; // Do not parse response code: it's unknown
		//case 301:
		//case 302:
		//case 303:
		case 304:
			log(d, "File is not modified (304)");
			onComplete(d); return;
		case 307:
			String newurl = connection.getHeaderField("Location"); connection.disconnect();
			if(newurl == null)
				throw new DownloaderError("Redirection is required but field \"Location\" is empty", true);
			
			dlog("Responce code is "+code+". Redirecting to: "+newurl);			
			d.setURL(newurl); download(d, 1, max); return;
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
		default:
			if(code < 200 || code > 299)
				throw new DownloaderError("Illegal response code ("+code+")", true);
		}
		
		InputStream in = connection.getInputStream();
		
		File temp = FileUtil.makeTemp(new File(d.getDestination().getAbsolutePath() + ".tlauncherdownload"));
		OutputStream out = new FileOutputStream(temp);
		
		long
			read = 0L,
			length = connection.getContentLength(),
			downloaded_s = System.currentTimeMillis(),
			speed_s = downloaded_s,
			downloaded_e, speed_e;
		
		// Speed in kb/s:
		// read (bytes) / time (ms)
		
		// How does it works:
		// Every second calculates speed using general variables: (read at all / download time)
		
		byte[] buffer = new byte[65536];
		
		int curread = in.read(buffer);
		while(curread > 0){
			if(!launched){
				out.close();
				throw new DownloaderStoppedException();
			}
			
			read += curread;
			
			out.write(buffer, 0, curread);
			
			curread = in.read(buffer);
			
			if(curread == -1)
				break;
			
			speed_e = System.currentTimeMillis() - speed_s;
			if(speed_e < 50) continue;
			
			speed_s = System.currentTimeMillis(); // "clearing" variable for the next calculating
			downloaded_e = speed_s - downloaded_s; // System.currentTimeMillis() - downloaded_s: calculating general download time.
			
			double
				curdone = read / (float) length,
				curspeed = (read / (double) downloaded_e);
			
			onProgress(curread, curdone, curspeed);
		}
		downloaded_e = System.currentTimeMillis() - downloaded_s;
		
		double downloadSpeed = (downloaded_e != 0)? (read / (double) downloaded_e) : 0.0; 
		
		in.close();
		out.close();
		connection.disconnect();
		
		if(!temp.renameTo(file)){
			FileUtil.copyFile(temp, file, true);
			FileUtil.deleteFile(temp);
		}
		
		File[] copies = d.getAdditionalDestinations();
		if(copies != null && copies.length > 0){
			dlog("Found additional destinations. Copying...");
			for(File copy : copies){
				dlog("Copying "+copy+"...");
				FileUtil.copyFile(file, copy, d.isForced());
				dlog(d, "Success!");
			}
			dlog("Copying completed.");
		}
		
		d.setTime(downloaded_e);
		d.setSize(read);
		
		log(d, "Downloaded in "+ d.getTime() +" ms. at "+downloadSpeed+" kb/s ["+attempt+"/"+max+";"+d.getFast()+"]");
		
		if(!launched)
			throw new DownloaderStoppedException();
		
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
	public double getSpeed(){ return speed; }
	public int getProgress(){ return progress; }
	public int getRemain(){ return remain; }
	public int getDone(){ return done; }
	
	public void startLaunch(){ launched = true; }
	public void stopLaunch(){ launched = false; }
	
	// Events
	private void onStart(Downloadable d){
		d.onStart();
		if(d.hasContainter()) d.getContainer().onStart();
		
		fd.onStart(id, d);
	}
	private void onAbort(Downloadable d){
		d.onAbort();
		if(d.hasContainter()) d.getContainer().onAbort();
	}
	private void onComplete(Downloadable d){
		d.onComplete();
		if(d.hasContainter()) d.getContainer().onFileComplete();
		
		--remain; ++done;
		progress = (int) (done * each);
		
		fd.onProgress(id, progress, speed);
		fd.onComplete(id, d);
	}
	private void onError(Downloadable d, Throwable err){
		error = err; d.setError(err); d.onError();
		if(d.hasContainter()) d.getContainer().onError();
		fd.onError(id, d);
	}
	private void onProgress(int curread, double curdone, double curspeed){
		++avi; if(avi == av) avi = 0;
		av_speed[avi] = curspeed;
		
		int old_progress = progress;
		
		progress = (int) ((done * each) + (curdone * each));
		speed = U.getAverage(av_speed);
				
		if(progress == old_progress) return;
		
		fd.onProgress(id, progress, curspeed);
	}
	
	// Util
	private void dlog(Object... message){
		String prefix = "["+name+"DT #"+id+"]";
		U.log(prefix, message);
	}
	private void log(Downloadable d, Object... message){
		DownloadableContainer c = d.getContainer();
		String prefix = "["+name+"DT #"+id+"]\n> " + d.getURL() + "\n> ";
		
		if(c != null && c.hasConsole()) c.log(prefix, message);
		U.log(prefix, message);
	}
	private void check(){
		if(available) return;
		throw new IllegalStateException("DownloaderThread (#"+id+") is unavailable!");
	}
	private void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){}
	}
	
	private class DownloaderStoppedException extends RuntimeException {
		private static final long serialVersionUID = 1383043531539603476L;
	}
}
