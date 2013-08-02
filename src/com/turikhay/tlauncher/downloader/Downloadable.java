package com.turikhay.tlauncher.downloader;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.turikhay.tlauncher.util.FileUtil;

public class Downloadable {
	private URL url;
	private File destination;
	private String md5, filename;
	private Throwable error;
	private DownloadableContainer container;
	private Runnable onComplete;
	private boolean forced;
	
	public Downloadable(URL url, File destination, boolean force){
		this.url = url;
		this.destination = destination;
		this.forced = force;
	}
	
	public Downloadable(String url, File destination, boolean force) throws MalformedURLException {
		this.url = new URL(url);
		this.destination = destination;
		this.forced = force;
	}
	
	public Downloadable(URL url, File destination) {
		this(url, destination, false);
	}
	
	public Downloadable(String url, File destination) throws MalformedURLException {
		this(url, destination, false);
	}
	
	public URL getURL(){ return url; }
	public File getDestination(){ return destination; }
	public String getMD5(){ if(md5 == null) return (md5 = FileUtil.getMD5Checksum(destination)); return md5; }
	public String getFilename(){ if(filename == null) return (filename = FileUtil.getFilename(url)); return filename; }
	public Throwable getError(){ return error; }
	public boolean isForced(){ return forced; }
	public boolean hasContainter(){ return (container != null); }
	public DownloadableContainer getContainer(){ return container; }
	public void onComplete(){ if(onComplete != null) onComplete.run(); }
	
	void setError(Throwable e){ this.error = e; }
	void setContainer(DownloadableContainer newcontainer){ container = newcontainer; }
	public void setOnComplete(Runnable r){ onComplete = r; }
	
	HttpURLConnection makeConnection() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		connection.setRequestProperty("Expires", "0");
		connection.setRequestProperty("Pragma", "no-cache");		
		if (getMD5() != null) connection.setRequestProperty("If-None-Match", md5);

		connection.connect();

		return connection;
	}
	
	public static String getEtag(String etag) {
		if (etag == null)
			etag = "-";
		else if ((etag.startsWith("\"")) && (etag.endsWith("\"")))
		{
			etag = etag.substring(1, etag.length() - 1);
		}

		return etag;
	}
}
