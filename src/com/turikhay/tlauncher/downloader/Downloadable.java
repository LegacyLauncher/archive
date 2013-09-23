package com.turikhay.tlauncher.downloader;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.launcher_.Http;

import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.util.FileUtil;

public class Downloadable {
	public final static int
		CONNECTION_TIMEOUT = 30000,
		READ_TIMEOUT = 10000;
	
	private URL url;
	private File destination;
	private Throwable error;
	private DownloadableContainer container;
	private DownloadableHandler handler;
	private boolean forced;
	
	public Downloadable(String url, File destination, boolean force) throws MalformedURLException {
		this.url = new URL(Http.encode(url));
		this.destination = destination;
		this.forced = force;
	}
	
	public Downloadable(URL url, boolean force){
		this.url = url;
	}
	
	public Downloadable(String url, File destination) throws MalformedURLException {
		this(url, destination, false);
	}
	
	public Downloadable(URL url){
		this(url, false);
	}
	
	public URL getURL(){ return url; }
	public File getDestination(){ return destination; }
	public String getMD5(){ return FileUtil.getMD5Checksum(destination); }
	public String getFilename(){ return Http.decode(FileUtil.getFilename(url)); }
	public Throwable getError(){ return error; }
	public boolean isForced(){ return forced; }
	public boolean hasContainter(){ return (container != null); }
	public DownloadableContainer getContainer(){ return container; }
	
	public void onStart(){ if(handler != null) handler.onStart(); }
	public void onComplete(){ if(handler != null) handler.onComplete(); }
	public void onError(){ if(handler != null) handler.onCompleteError(); }
	
	public void setHandler(DownloadableHandler newhandler){ handler = newhandler; }	
	void setError(Throwable e){ this.error = e; }
	void setContainer(DownloadableContainer newcontainer){ container = newcontainer; }
	
	public void setURL(URL newurl){ this.url = newurl; }
	public void setURL(String newurl) throws MalformedURLException { this.url = new URL(newurl); }
	public void setDestination(File newdestination){ this.destination = newdestination; }
	public void setForced(boolean newforced){ this.forced = newforced; }
	
	public HttpURLConnection makeConnection() throws IOException {
		String md5 = getMD5();
		HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
		
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		connection.setRequestProperty("Expires", "0");
		connection.setRequestProperty("Pragma", "no-cache");		
		if (md5 != null) connection.setRequestProperty("If-None-Match", md5);

		connection.connect();

		return connection;
	}
	
	public String toString(){
		if(url != null)
			return url.toExternalForm();
		return null;
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
