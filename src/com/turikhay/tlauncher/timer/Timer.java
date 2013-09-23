package com.turikhay.tlauncher.timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.util.U;

public class Timer extends Thread {
	private Map<String, TimerTask> tasks = new HashMap<String, TimerTask>();
	private List<String> remove = new ArrayList<String>();
	private boolean available = true, tasks_ = true;
	
	public void run(){
		long tick = 0;
		while(available){ ++tick;
			tasks_ = false;
			for(Entry<String, TimerTask> entry : tasks.entrySet()){
				String name = entry.getKey();
				TimerTask task = entry.getValue();
				
				if(tick % task.getTicks() != 0) continue;
				if(!task.isRepeating()) remove.add(name);
				
				try{ task.run(); }catch(Exception e){ throw new TLauncherException("Exception in task \""+name+"\"", e); }
			}
			
			for(String cr : remove){
				U.log("[TIMER] Removing "+cr);
				tasks.remove(cr);
			}
			remove.clear();
			
			tasks_ = true;
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new TLauncherException("Timer cannot sleep.", e);
			}
		}
		
		run();
	}
	
	public void add(String name, TimerTask task){
		while(!tasks_) sleepFor(100);
		
		tasks_ = false;
		if(tasks.containsKey(name)) throw new TLauncherException("Tried to add task with the same name");
		tasks.put(name, task);
		tasks_ = true;
	}
	
	public void remove(String name){
		while(!tasks_) sleepFor(100);
		
		tasks_ = false;
		tasks.remove(name);
		tasks_ = true;
	}
	
	private void sleepFor(long millis){
		try{ Thread.sleep(millis); }catch(Exception e){}
	}
}
