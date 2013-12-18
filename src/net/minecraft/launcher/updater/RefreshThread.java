package net.minecraft.launcher.updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RefreshThread {
	private static List<RefreshThread> threads = Collections.synchronizedList(new ArrayList<RefreshThread>());
	
	private final VersionList list;
	private boolean cancelled;
	
	RefreshThread(VersionList list){
		this.list = list;
	}
	
	public void refreshVersions() throws IOException{
		this.list.refreshVersions();
	}
	
	public void cancelRefresh(){
		this.cancelled = true;
	}
	
	public boolean isCancelled(){
		return cancelled;
	}
	
	public static void cancelAll(){
		synchronized(threads){
			Iterator<RefreshThread> i = threads.iterator();
			while (i.hasNext()){
				i.next().cancelRefresh();
				i.remove();
			}
		}
	}

}
