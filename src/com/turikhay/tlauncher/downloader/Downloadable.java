package com.turikhay.tlauncher.downloader;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launcher.Http;

import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public class Downloadable {	
	public static final String DEFAULT_CHECKSUM_ALGORITHM = "SHA-1";
	private List<DownloadableHandler> handlers = new ArrayList<DownloadableHandler>();
	
	private URL url;
	private File destination;
	private File[] copies;
	private DownloadableContainer container;
	private boolean forced, fast;
	private long time, size;
	
	protected Throwable error;
	
	public Downloadable(URL url, File destination, File[] copies, boolean force) {
		this.url = url;
		this.destination = destination;
		this.copies = copies;
		this.forced = force;
	}
	public Downloadable(String url, File destination, File[] copies, boolean force) throws MalformedURLException {
		this(new URL(Http.encode(url)), destination, copies, force);
	}
	
	public Downloadable(String url, File destination, boolean force) throws MalformedURLException {
		this(url, destination, new File[0], force);
	}
	
	public Downloadable(URL url, boolean force){
		this.url = url;
        this.forced = force;
	}
	
	public Downloadable(String url, File destination) throws MalformedURLException {
		this(url, destination, false);
	}
	
	public Downloadable(URL url){
		this(url, false);
	}
	
	public Downloadable(URL url, File destination){
		this(url, destination, null, false);
	}
	
	public URL getURL(){ return url; }
	public File getDestination(){ return destination; }
	public File[] getAdditionalDestinations(){ return copies; }
	public String getHash(String algorithm){ return FileUtil.getChecksum(destination, algorithm); }
	public String getFilename(){ return Http.decode(FileUtil.getFilename(url)); }
	public Throwable getError(){ return error; }
	public boolean isForced(){ return forced; }
	public boolean hasContainter(){ return (container != null); }
	public DownloadableContainer getContainer(){ return container; }
	public long getTime(){ return time; }
	public long getSize(){ return size; }
	public boolean getFast(){ return fast; }
	
	public void onStart(){ for(DownloadableHandler h : handlers) h.onStart(); }
	public void onComplete(){ for(DownloadableHandler h : handlers) h.onComplete(); }
	public void onError(){ for(DownloadableHandler h : handlers) h.onCompleteError(); }
	public void onAbort(){ for(DownloadableHandler h : handlers) h.onAbort(); }
	
	public void addHandler(DownloadableHandler newhandler){ handlers.add(newhandler); }
	void setError(Throwable e){ this.error = e; }
	void setContainer(DownloadableContainer newcontainer){ container = newcontainer; }
	
	public void setURL(URL newurl){ this.url = newurl; }
	public void setURL(String newurl) throws MalformedURLException { this.url = new URL(newurl); }
	public void setDestination(File newdestination){ this.destination = newdestination; }
	public void setAdditionalDestinations(File[] newdestinations){ this.copies = newdestinations; }
	public void setForced(boolean newforced){ this.forced = newforced; }
	public void setTime(long ms){ this.time = ms; }
	public void setSize(long b){ this.size = b; }
	public void setFast(boolean newfast){ this.fast = newfast; }
	
	public HttpURLConnection makeConnection() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
		setUp(connection, false);
		
		String md5 = getHash("MD5");
		if (md5 != null)
			connection.setRequestProperty("If-None-Match", md5);

		return connection;
	}
	
	public String toString(){
		String
		r = "{";
		r += "url=" + ((url == null)? null : url.toExternalForm());
		r += ",destination=" + destination;
		r += ",additionaldestinations=" + U.toLog((Object[]) copies);
		r += ",error=" + error;
		r += ",container=" + container;
		r += ",handlers=" + U.toLog(handlers);
		r += ",forced=" + forced;
		r += "}";
		return r;
	}
	
	public static URLConnection setUp(URLConnection connection, boolean fake){
        connection.setConnectTimeout(U.getConnectionTimeout());
        connection.setReadTimeout(U.getReadTimeout());

		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		connection.setRequestProperty("Expires", "0");
		connection.setRequestProperty("Pragma", "no-cache");
		
		if(!fake) return connection;
		
		connection.setRequestProperty("Accept", "text/html, application/xml;q=0.9, application/xhtml xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1");
		connection.setRequestProperty("Accept-Language", "en");
		connection.setRequestProperty("Accept-Charset", "iso-8859-1, utf-8, utf-16, *;q=0.1");
		connection.setRequestProperty("Accept-Encoding", "deflate, gzip, x-gzip, identity, *;q=0");
		connection.setRequestProperty("User-Agent", "Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.16");
		
		return connection;
	}
	
	public static URLConnection setUp(URLConnection connection){ return setUp(connection, false); }
	
	public static String getEtag(String etag) {
		if (etag == null) return "-";
		
		if ((etag.startsWith("\"")) && (etag.endsWith("\"")))
			return etag.substring(1, etag.length() - 1);

		return etag;
	}
}
