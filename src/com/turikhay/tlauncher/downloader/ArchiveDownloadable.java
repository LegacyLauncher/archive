package com.turikhay.tlauncher.downloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.turikhay.util.FileUtil;

public class ArchiveDownloadable extends Downloadable {
	protected File folder;
	
	public ArchiveDownloadable(URL url, File folder, boolean force) {
		super(url, createTemp(folder), null, force);
		init(folder);
	}
	
	public ArchiveDownloadable(String url, File folder, boolean force) throws MalformedURLException {
		super(url, createTemp(folder), force);
		init(folder);
	}
	
	public void onComplete(){
		try{
			FileUtil.unZip(getDestination(), folder, isForced());
			if(!getDestination().delete())
				throw new IOException("Cannot remove temp file!");
		}
		catch(IOException e){
			this.error = e;
			this.onError();
			
			return;
		}
		super.onComplete();
	}
	
	private void init(File folder){
		if(folder == null)
			throw new NullPointerException("Folder is NULL!");
		
		this.folder = folder;
	}
	
	protected static File createTemp(File folder){
		File parent = folder.getParentFile();
		if(parent == null) parent = folder;
		
		File ret = new File(parent, System.currentTimeMillis() + ".tlauncher.unzip");
		ret.deleteOnExit();
		
		return ret;
	}
}
