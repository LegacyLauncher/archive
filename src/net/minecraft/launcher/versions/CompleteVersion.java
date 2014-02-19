package net.minecraft.launcher.versions;

import java.io.File;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.minecraft.repository.VersionRepository;
import com.turikhay.util.U;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;

public class CompleteVersion implements Version, Cloneable {
	private String id, original_id;
	
	private Date time;
	private Date releaseTime;
	
	private ReleaseType type;
	
	private String jvmArguments;
	private String minecraftArguments;
	private String mainClass;
	
	private List<Library> libraries;
	private List<Rule> rules;
	private List<String> unnecessaryEntries;
	
	private int minimumLauncherVersion;
	private int tlauncherVersion;
	
	private String incompatibilityReason;
	private String assets;
	
	private VersionRepository source;
	private VersionList list;

	@Override
	public String getID() {
		return id;
	}
	
	public void setID(String id) {
		if(id == null || id.isEmpty()) throw new IllegalArgumentException("ID is NULL or empty");
		this.id = id;
	}

	@Override
	public ReleaseType getReleaseType() {
		return type;
	}

	@Override
	public VersionRepository getSource() {
		return source;
	}
	
	@Override
	public void setSource(VersionRepository repository){
		if(repository == null)
			throw new NullPointerException();
		
		this.source = repository;
	}

	@Override
	public Date getUpdatedTime() {
		return time;
	}
	
	public void setUpdatedTime(Date time) {
		if(time == null) throw new NullPointerException("Time is NULL!");
		this.time = time;
	}

	@Override
	public Date getReleaseTime() {
		return releaseTime;
	}
	
	@Override
	public VersionList getVersionList() {
		return list;
	}

	@Override
	public void setVersionList(VersionList list) {
		if(list == null)
			throw new NullPointerException("VersionList cannot be NULL!");
		
		this.list = list;
	}
	
	public String getOriginal() {
		return original_id;
	}
	
	public String getJVMArguments() {
		return jvmArguments;
	}
	
	public String getMinecraftArguments() {
		return minecraftArguments;
	}
	
	public String getMainClass(){
		return mainClass;
	}
	
	public List<Library> getLibraries() {
		return Collections.unmodifiableList(libraries);
	}
	
	public List<Rule> getRules() {
		return Collections.unmodifiableList(rules);
	}
	
	public List<String> getRemovableEntries(){
		return unnecessaryEntries;
	}
	
	public int getMinimumLauncherVersion() {
		return minimumLauncherVersion;
	}
	
	public int getMinimumCustomLauncherVersion() {
		return tlauncherVersion;
	}
	
	public String getIncompatibilityReason() {
		return incompatibilityReason;
	}
	
	public String getAssets() {
		return assets;
	}
	
	public boolean equals(Object o){
		if(this == o) return true;
		if(o == null) return false;
		if(this.hashCode() == o.hashCode()) return true;
		
		if(!(o instanceof Version)) return false;
		
		Version compare = (Version) o;
		if(compare.getID() == null) return false;
		
		return compare.getID().equals(id);
	}
	
	public String toString() {
		return getClass().getSimpleName() + "{id='"+id+"', time="+ time +", release="+ releaseTime +", type="+ type +", class="+ mainClass +", minimumVersion="+ minimumLauncherVersion +", assets='"+ assets +"', source="+ source +", list="+ list +", libraries="+ libraries +"}";
	}
	
	public File getFile(File base){
		return new File(base, "versions/" + getID() + "/" + getID() + ".jar");
	}
	
	public boolean appliesToCurrentEnvironment() {
		if (this.rules == null) return true;

		for (Rule rule : this.rules) {
			Rule.Action action = rule.getAppliedAction();
			
			if (action == Rule.Action.DISALLOW)
				return false;
		}

		return true;
	}
	
	public Collection<Library> getRelevantLibraries() {
		List<Library> result = new ArrayList<Library>();

		for(Library library : this.libraries)
			if(library.appliesToCurrentEnvironment())
				result.add(library);

		return result;
	}
	
	public Collection<File> getClassPath(OperatingSystem os, File base) {
		Collection<Library> libraries = getRelevantLibraries();
		Collection<File> result = new ArrayList<File>();

		for(Library library : libraries) {
			if(library.getNatives() == null)
				result.add(new File(base, "libraries/" + library.getArtifactPath()));
		}

		result.add(new File(base, "versions/" + getID() + "/" + getID() + ".jar"));

		return result;
	}
	
	public Collection<String> getNatives(OperatingSystem os) {
		Collection<Library> libraries = getRelevantLibraries();
		Collection<String> result = new ArrayList<String>();

		for(Library library : libraries) {
			Map<OperatingSystem, String> natives = library.getNatives();

			if(natives != null && natives.containsKey(os))
				result.add("libraries/" + library.getArtifactPath( natives.get(os) ));
		}

		return result;
	}
	
	public Set<String> getRequiredFiles(OperatingSystem os) {
		Set<String> neededFiles = new HashSet<String>();

		for (Library library : getRelevantLibraries()) {
			if (library.getNatives() != null) {
				String natives = library.getNatives().get(os);
				
				if (natives != null)
					neededFiles.add("libraries/" + library.getArtifactPath(natives));
				
		      } else {
		    	  neededFiles.add("libraries/" + library.getArtifactPath());
		      }
		}

		return neededFiles;
	}
	
	public Set<Downloadable> getRequiredDownloadables(OperatingSystem os, File targetDirectory, boolean force) throws MalformedURLException {
		Set<Downloadable> neededFiles = new HashSet<Downloadable>();

		for(Library library : getRelevantLibraries()) {
			String file = null;

			if(library.getNatives() != null){
				String natives = library.getNatives().get(os);
				if(natives != null)
					file = library.getArtifactPath(natives);
			} else {
				file = library.getArtifactPath();
			}

			if (file == null) continue;
		      
			File local = new File(targetDirectory, "libraries/" + file);
			if(!force && local.isFile()) continue;
			
			String url = library.getURL(source, os);  
	    	// TODO set repository to downloadable.
			neededFiles.add(new Downloadable(url, local, force));
		}

		return neededFiles;
	}
	
	public Set<Downloadable> getRequiredDownloadables(File targetDirectory, boolean force) throws MalformedURLException {
		return getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), targetDirectory, force);
	}
	
	  public Collection<String> getExtractFiles(OperatingSystem os) {
		    Collection<Library> libraries = getRelevantLibraries();
		    Collection<String> result = new ArrayList<String>();

		    for (Library library : libraries) {
		      Map<OperatingSystem, String> natives = library.getNatives();

		      if ((natives != null) && (natives.containsKey(os))) {
		        result.add("libraries/" + library.getArtifactPath(natives.get(os)));
		      }
		    }

		    return result;
		  }
	
	public static class CompleteVersionSerializer implements JsonSerializer<CompleteVersion>, JsonDeserializer<CompleteVersion> {
		private final Gson defaultContext;
		
		public CompleteVersionSerializer() {
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
			builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
			builder.enableComplexMapKeySerialization();
			builder.setPrettyPrinting();

			this.defaultContext = builder.create();
		}
		
		@Override
		public CompleteVersion deserialize(JsonElement elem, Type type, JsonDeserializationContext context) throws JsonParseException {
			CompleteVersion version = (CompleteVersion) defaultContext.fromJson(elem, CompleteVersion.class);
			
			if(version.id == null)
				throw new JsonParseException("Version ID is NULL!");
			
			if(version.type == null)
				version.type = ReleaseType.UNKNOWN;
			
			if(version.source == null)
				version.source = VersionRepository.LOCAL;
			
			if(version.time == null)
				version.time = new Date(0);
			
			if(version.assets == null)
				version.assets = "legacy";
			
			return version;
		}

		@Override
		public JsonElement serialize(CompleteVersion version0, Type type, JsonSerializationContext context) {
			CompleteVersion version;
			
			try {
				version = (CompleteVersion) version0.clone();
			} catch (CloneNotSupportedException e) {
				U.log("Cloning of CompleteVersion is not supported O_o", e);
				return defaultContext.toJsonTree(version0, type);
			}
			
			version.list = null;
			
			return defaultContext.toJsonTree(version, type);
		}
	}
}
