package com.turikhay.tlauncher.minecraft.profiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.events.ProfileListener;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;

public class ProfileLoader {	
	public final static File DEFAULT_PROFILE_STORAGE = MinecraftUtil.getSystemRelatedFile("tlauncher.profiles.list");
	public final static String DEFAULT_PROFILE_FILENAME = "launcher_profiles.json";
	
	private final List<ProfileManager> managers;
	private final List<ProfileListener> listeners;
	
	private ProfileManager selected;
	private File storageFile;
	
	public ProfileLoader(File storage) throws IOException {		
		this.managers = Collections.synchronizedList(new ArrayList<ProfileManager>());
		this.listeners = Collections.synchronizedList(new ArrayList<ProfileListener>());
		
		create(storage);
	}
	
	public ProfileLoader() throws IOException {
		this(DEFAULT_PROFILE_STORAGE);
	}
	
	private void create(File storage) throws IOException {
		log("Creating ProfileLoader from file:", storage);
		
		if(storage == null)
			throw new NullPointerException("File is NULL!");
		
		this.storageFile = storage;
		if(FileUtil.createFile(storage)) log("File created!");
		
		String content = FileUtil.readFile(storage);
		if(content == null || content.length() == 0)
			content = writeDefault();
		
		List<File> files = new ArrayList<File>();
		String[] lines = content.split("\n");
		String line; int select = -1;
		
		for(int i = 0; i < lines.length ; i++){
			line = lines[i];
			
			boolean selected = false;
			if(line.startsWith(">")){ selected = true; line = line.substring(1); } 
			if(line.trim().isEmpty()){ log("Empty line:", i+1); continue; }
			
			File dir = new File(line);
			if(dir.isDirectory()){
				log("Found directory:", dir);
				
				files.add(new File(dir, DEFAULT_PROFILE_FILENAME));
				if(selected) select = i;
			}
		}
		
		for(int i=0;i<files.size();i++)
			this.managers.add( new ProfileManager(this, TLauncher.getClientToken(), files.get(i), true) );
		
		this.selected = (managers.size() > 0)? this.managers.get((select == -1)? 0 : select) : null;
	}
	
	public ProfileManager getSelected(){
		return selected;
	}
	
	public boolean setSelected(ProfileManager pm){
		for(ProfileManager cpm : this.managers)
			if(cpm.equals(pm)){
				this.selected = pm;
				return true;
			}
		return false;
	}
	
	public List<ProfileManager> getManagers(){
		return Collections.unmodifiableList(managers);
	}
	
	public void add(ProfileManager... pms){
		for(ProfileManager pm : pms)
			if(this.managers.contains(pm))
				throw new IllegalArgumentException("ProfileLoader already contains specified manager: " + pm);
			else
				this.managers.add(pm);
	}
	
	public void remove(ProfileManager... pms){
		for(ProfileManager pm : pms)
			if(!this.managers.contains(pm))
				throw new IllegalArgumentException("ProfileLoader does not contain specified manager: " + pm);
			else
				this.managers.add(pm);
	}
	
	public void removeAll(){
		this.managers.clear();
	}
	
	public void addListener(ProfileListener l){
		this.listeners.add(l);
	}
	
	public void removeListener(ProfileListener l){
		this.listeners.remove(l);
	}
	
	public void save() throws IOException {
		StringBuilder s = new StringBuilder();
		boolean first = true;
		
		synchronized(this.managers){
			for(ProfileManager pm : this.managers){
				if(!first) s.append("\n"); else first = false;
				if(pm.equals(selected)) s.append(">");
				s.append(pm.getFile().getAbsolutePath());
			}
		}
		
		FileUtil.writeFile(storageFile, s.toString());
	}
	
	public void loadProfiles() throws IOException {
		synchronized(this.managers){
			for(ProfileManager pm : this.managers)
				pm.loadProfiles();
		}
	}
	
	public void saveProfiles() throws IOException {
		synchronized(this.managers){
			for(ProfileManager pm : this.managers)
				pm.saveProfiles();
		}
	}
	
	private String writeDefault() throws IOException {
		String path = MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath();
		
		FileUtil.writeFile(this.storageFile, ">" + path);
		return path;
	}
	void onRefresh(final ProfileManager pm) {
		synchronized(this.listeners){
			for(ProfileListener pl : this.listeners)
				pl.onProfilesRefreshed(pm);
		}
	}
	private void log(Object... o){ U.log("[ProfileLoader]", o); }
}
