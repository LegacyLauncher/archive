package net.minecraft.launcher.updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.CompleteVersion.CompleteVersionSerializer;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.launcher.versions.Version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.turikhay.util.Time;
import com.turikhay.util.U;

public abstract class VersionList {
	protected final Gson gson;
	private final Map<String, Version> byName;
	private final List<Version> versions;
	  
	public VersionList() {
		this.versions = new ArrayList<Version>();
		this.byName = new HashMap<String, Version>();
		  
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
		builder.registerTypeAdapter(CompleteVersion.class, new CompleteVersionSerializer());
		builder.enableComplexMapKeySerialization();
		builder.setPrettyPrinting();

		this.gson = builder.create();
	}
	  
	public List<Version> getVersions() {
		  return Collections.unmodifiableList(versions);
	}
	  
	public Version getVersion(String name) {
		if(name == null || name.isEmpty())
			throw new IllegalArgumentException("Name cannot be NULL or empty");
		
		return byName.get(name);
	}
	  
	public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
		if(version instanceof CompleteVersion) return (CompleteVersion) version;
		  
		if(version == null)
			throw new NullPointerException("Version cannot be NULL!");
		  
		CompleteVersion complete = gson.fromJson(
				getUrl("versions/" + version.getID() + "/" + version.getID() + ".json"), CompleteVersion.class);
		
		complete.setID( version.getID() ); // IDs should be the same
		complete.setVersionList(this);
		  
		Collections.replaceAll(this.versions, version, complete);
		  
		return complete;
	}
	
	public CompleteVersion getCompleteVersion(String name) throws JsonSyntaxException, IOException {
		Version version = getVersion(name);
		if(version == null) return null;
		
		return getCompleteVersion(version);
	}
	
	public RawVersionList getRawList() throws IOException {
		Object lock = new Object();
		Time.start(lock);
		  
		RawVersionList list = this.gson.fromJson(getUrl("versions/versions.json"), RawVersionList.class);
		
		for(PartialVersion version : list.versions)
			version.setVersionList(this);
		  
		log("Got in",Time.stop(lock),"ms");
		
		return list;
	}
	
	public void refreshVersions(RawVersionList versionList) {
		clearCache();

		for(Version version : versionList.getVersions()){
			this.versions.add(version);
			this.byName.put(version.getID(), version);
		}
	}
	
	public void refreshVersions() throws IOException {
		refreshVersions(getRawList());
	}
	
	public CompleteVersion addVersion(CompleteVersion version) {
		if (version.getID() == null)
			throw new IllegalArgumentException("Cannot add blank version");
		
		if (getVersion(version.getID()) != null){
			log("Version '" + version.getID() + "' is already tracked");
			return version;
		}

		versions.add(version);
	    byName.put(version.getID(), version);

	    return version;
	}
	
	public void removeVersion(Version version) {
		if(version == null)
			throw new NullPointerException("Version cannot be NULL!");
		
		versions.remove(version);
		byName.remove(version);
	}
	
	public void removeVersion(String name) {
		Version version = getVersion(name);
		if(version == null) return;
		removeVersion(version);
	}
	
	public String serializeVersion(CompleteVersion version) {
		if(version == null)
			throw new NullPointerException("CompleteVersion cannot be NULL!");
		
		return gson.toJson(version);
	}
	
	//
	public abstract boolean hasAllFiles(CompleteVersion paramCompleteVersion, OperatingSystem paramOperatingSystem);
	protected abstract String getUrl(String uri) throws IOException;
	//
	
	protected void clearCache() {
		byName.clear();
		versions.clear();
	}
	protected void log(Object...obj){ U.log("["+getClass().getSimpleName()+"]", obj); }
	
	public static class RawVersionList {
		private List<PartialVersion> versions = new ArrayList<PartialVersion>();
		private Map<ReleaseType, String> latest = new EnumMap<ReleaseType, String>(ReleaseType.class);

		public List<PartialVersion> getVersions() {
			return this.versions;
		}

		public Map<ReleaseType, String> getLatestVersions() {
			return this.latest;
		}
	}
}
