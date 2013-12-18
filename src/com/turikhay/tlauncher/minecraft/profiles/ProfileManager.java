package com.turikhay.tlauncher.minecraft.profiles;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.launcher.updater.versions.json.DateTypeAdapter;
import net.minecraft.launcher.updater.versions.json.FileTypeAdapter;
import net.minecraft.launcher.updater.versions.json.LowerCaseEnumTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.turikhay.tlauncher.minecraft.auth.AuthenticationDatabase;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public class ProfileManager
{
	public static final String DEFAULT_PROFILE_NAME = "TLauncher";
	private final JsonParser parser = new JsonParser();
	private final Gson gson;
	private final Map<String, Profile> profiles = new HashMap<String, Profile>();
	private final File profileFile;
	private final ProfileLoader loader;
	private String selectedProfile; private UUID clientToken;
	private AuthenticationDatabase authDatabase;
	
	public ProfileManager(ProfileLoader loader, UUID clientToken, File file) {
		this(loader, clientToken, file, false);
	}

	public ProfileManager(ProfileLoader loader, UUID clientToken, File file, boolean load) {
		this.loader = loader;
		this.profileFile = file;
		
		this.clientToken = clientToken;
    
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
		builder.registerTypeAdapter(File.class, new FileTypeAdapter());
		builder.registerTypeAdapter(AuthenticationDatabase.class, new AuthenticationDatabase.Serializer());
		builder.setPrettyPrinting();
    
		this.gson = builder.create();
		this.authDatabase = new AuthenticationDatabase();
		
		if(load)
			try {
				this.loadProfiles();
				this.saveProfiles();
			} catch (Throwable e) {
				U.log("Cannot load profiles! Ignoring, though.", e);
			}
	}

	public void saveProfiles() throws IOException {
		RawProfileList rawProfileList = new RawProfileList();
	  
		rawProfileList.profiles = this.profiles;
		rawProfileList.selectedProfile = getSelectedProfile().getName();
		rawProfileList.clientToken = clientToken;
		rawProfileList.authenticationDatabase = this.authDatabase;

		FileUtil.writeFile(this.profileFile, this.gson.toJson(rawProfileList));
	}

	public boolean loadProfiles() throws IOException {
		this.profiles.clear();
		this.selectedProfile = null;

		if (this.profileFile.isFile()) {
			JsonObject object = this.parser.parse(FileUtil.readFile(this.profileFile)).getAsJsonObject();
      
			if (object.has("clientToken"))
				this.clientToken = this.gson.fromJson(object.get("clientToken"), UUID.class);
      
			RawProfileList rawProfileList = this.gson.fromJson(object, RawProfileList.class);
			
			this.profiles.putAll(rawProfileList.profiles);
			this.selectedProfile = rawProfileList.selectedProfile;
			this.authDatabase = rawProfileList.authenticationDatabase;

			fireRefreshEvent();
			return true;
		}
    
		fireRefreshEvent();
		return false;
	}
	
	public File getFile(){
		return this.profileFile;
	}
	
	public ProfileLoader getLoader(){
		return this.loader;
	}

	public void fireRefreshEvent() {
		this.loader.onRefresh(this);
	}

	public Profile getSelectedProfile() {
		if ((this.selectedProfile == null) || (!this.profiles.containsKey(this.selectedProfile))) {
			if (this.profiles.get(DEFAULT_PROFILE_NAME) != null) {
				this.selectedProfile = DEFAULT_PROFILE_NAME;
			} else if (this.profiles.size() > 0) {
				this.selectedProfile = this.profiles.values().iterator().next().getName();
			} else {
				this.selectedProfile = DEFAULT_PROFILE_NAME;
				this.profiles.put(DEFAULT_PROFILE_NAME, new Profile(this.selectedProfile));
			}
		}

		return this.profiles.get(this.selectedProfile);
	}

	public Map<String, Profile> getProfiles() {
		return this.profiles;
	}
  
	public void addProfile(String name, Profile profile) {
		this.profiles.put(name, profile);
	}
  
	public void removeProfile(String name){
		this.profiles.remove(name);
	}

	public void setSelectedProfile(String selectedProfile) {
		boolean update = !this.selectedProfile.equals(selectedProfile);
		this.selectedProfile = selectedProfile;

		if (update)
			fireRefreshEvent();
	}

	public AuthenticationDatabase getAuthDatabase() {
		return this.authDatabase;
	}

	public void trimAuthDatabase() {
		Set<String> uuids = new HashSet<String>(this.authDatabase.getknownUUIDs());

		for (Profile profile : this.profiles.values()) {
			uuids.remove(profile.getPlayerUUID());
		}

		for (String uuid : uuids)
			this.authDatabase.removeUUID(uuid);
	}

	static class RawProfileList {
		Map<String, Profile> profiles = new HashMap<String, Profile>();
		String selectedProfile;
		UUID clientToken = UUID.randomUUID();
		AuthenticationDatabase authenticationDatabase = new AuthenticationDatabase();
	}
}
