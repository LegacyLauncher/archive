package com.turikhay.tlauncher.minecraft.repository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.turikhay.util.Time;
import com.turikhay.util.U;

import net.minecraft.launcher.Http;

public class Repository {
	public static final int DEFAULT_TIMEOUT = 5000;
	
	public final String name;
	public final List<String> repos;
	
	private int primaryTimeout, selected;
	private boolean isSelected;
	
	public Repository(String name, int timeout, String[] urls){
		if(name == null)
			throw new NullPointerException("Name is NULL!");
		
		if(name.isEmpty())
			throw new IllegalArgumentException("Name is empty!");
		
		if(urls == null)
			throw new NullPointerException("URL array is NULL!");
		
		this.name = name.toUpperCase();
		this.repos = new ArrayList<String>();
		
		this.setTimeout(timeout);
		Collections.addAll(repos, urls);
	}
	
	public Repository(String name, String[] urls){
		this(name, DEFAULT_TIMEOUT, urls);
	}
	
	public Repository(String name, int timeout){
		this(name, timeout, new String[0]);
	}
	
	public Repository(String name){
		this(name, DEFAULT_TIMEOUT);
	}
	
	public int getTimeout(){
		return primaryTimeout;
	}
	
	public int getSelected(){
		return selected;
	}
	
	public void setSelected(int pos){
		if(!isSelectable())
			throw new IllegalStateException();
		
		this.isSelected = true;
		this.selected = pos;
	}
	
	public String getSelectedRepo(){
		return repos.get(selected);
	}
	
	public String getRepo(int pos){
		return repos.get(pos);
	}
	
	public List<String> getList(){
		return repos;
	}
	
	public int getCount(){
		return repos.size();
	}
	
	public boolean isSelected(){
		return isSelected;
	}
	
	public boolean isSelectable(){
		return !repos.isEmpty();
	}
	
	public String getUrl(String uri, boolean selectPath) throws IOException {
		boolean canSelect = isSelectable();
		  
		if(!canSelect) return getRawUrl(uri);
		  
		boolean gotError = false;
		  
		if(!selectPath && isSelected())
			try{ return this.getRawUrl(uri); }
			catch(IOException e){	  		  
				gotError = true;
				log("Cannot get required URL, reselecting path.");
			}
		  
		log("Selecting relevant path...");
		  
		Object lock = new Object();
		  
		IOException e = null;
		int i = 0, attempt = 0, exclude = (gotError)? getSelected() : -1;
		
		while(i < 3){
			++i;
			int timeout = primaryTimeout * i;
				
			for(int x=0;x<getCount();x++){
				if(i == 1 && x == exclude) continue; // Exclude bad path at the first try
				  
				++attempt;
				log("Attempt #"+attempt+"; timeout: "+timeout+" ms; url: "+getRepo(x));
					
				Time.start(lock);
					
				try {
					String result = Http.performGet(new URL(getRepo(x) + uri), timeout, timeout);
					setSelected(x);
					  
					log("Success: Reached the repo in", Time.stop(lock), "ms.");
					return result;
					
				} catch (IOException e0) {
					log("Failed: Repo is not reachable!");
					e = e0;
				}
				  
				Time.stop(lock);  
			}
		}
		  
		log("Failed: All repos are unreachable.");
		throw e;
	}
	  
	public String getUrl(String uri) throws IOException {
		return this.getUrl(uri, false);
	}
	  
	public String getRawUrl(String uri) throws IOException {
		String url = getSelectedRepo() + Http.encode(uri);
		  
		try{ return Http.performGet(new URL(url)); }
		catch(IOException e){
			log("Cannot get raw:", url);
			throw e;
		}
	}
	
	public String toString(){
		return name.toLowerCase();
	}
	
	public void setTimeout(int ms){
		if(ms < 0)
			throw new IllegalArgumentException("Negative timeout: " + ms);
		
		this.primaryTimeout = ms;
	}
	
	protected void log(Object...obj){ U.log("[REPO]["+name+"]", obj); }
}
