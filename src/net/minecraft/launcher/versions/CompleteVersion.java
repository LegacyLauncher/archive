package net.minecraft.launcher.versions;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.turikhay.tlauncher.downloader.Downloadable;

import net.minecraft.launcher.OperatingSystem;

public class CompleteVersion
  implements Version
{
  private String original_id, id, url;
  private Date time;
  private Date releaseTime;
  private ReleaseType type;
  private String jvmArguments;
  private String minecraftArguments;
  private List<Library> libraries;
  private String mainClass;
  private int minimumLauncherVersion;
  private int tlauncherVersion;
  private String incompatibilityReason;
  private List<Rule> rules;
  private List<String> unnecessaryEntries;
  private String assets;

  public String getId()
  {
    return this.id;
  }

  public ReleaseType getType()
  {
    return this.type;
  }

  public Date getUpdatedTime()
  {
    return this.time;
  }

  public Date getReleaseTime()
  {
    return this.releaseTime;
  }

  public Collection<Library> getLibraries() {
    return this.libraries;
  }

  public String getMainClass() {
    return this.mainClass;
  }

  public void setUpdatedTime(Date time)
  {
    if (time == null) throw new IllegalArgumentException("Time cannot be null");
    this.time = time;
  }

  public void setReleaseTime(Date time)
  {
    if (time == null) throw new IllegalArgumentException("Time cannot be null");
    this.releaseTime = time;
  }

  public void setType(ReleaseType type)
  {
    if (type == null) throw new IllegalArgumentException("Release type cannot be null");
    this.type = type;
  }

  public void setMainClass(String mainClass) {
    if ((mainClass == null) || (mainClass.length() == 0)) throw new IllegalArgumentException("Main class cannot be null or empty");
    this.mainClass = mainClass;
  }

  public Collection<Library> getRelevantLibraries() {
    List<Library> result = new ArrayList<Library>();

    for (Library library : this.libraries) {
      if (library.appliesToCurrentEnvironment()) {
        result.add(library);
      }
    }

    return result;
  }

  public Collection<File> getClassPath(OperatingSystem os, File base) {
    Collection<Library> libraries = getRelevantLibraries();
    Collection<File> result = new ArrayList<File>();

    for (Library library : libraries) {
      if (library.getNatives() == null) {
        result.add(new File(base, "libraries/" + library.getArtifactPath()));
      }
    }

    result.add(getJARFile(base));

    return result;
  }
  
  public File getJARFile(File base){
	  return new File(base, "versions/" + getId() + "/" + getId() + ".jar");
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

  public Set<String> getRequiredFiles(OperatingSystem os) {
    Set<String> neededFiles = new HashSet<String>();

    for (Library library : getRelevantLibraries()) {
      if (library.getNatives() != null) {
        String natives = (String)library.getNatives().get(os);
        if (natives != null) neededFiles.add("libraries/" + library.getArtifactPath(natives)); 
      }
      else { neededFiles.add("libraries/" + library.getArtifactPath()); }
    }

    return neededFiles;
  }

  public Set<Downloadable> getRequiredDownloadables(OperatingSystem os, VersionSource source, File targetDirectory, boolean force) throws MalformedURLException {
    Set<Downloadable> neededFiles = new HashSet<Downloadable>();

    for (Library library : getRelevantLibraries()) {
      String file = null;

      if (library.getNatives() != null) {
        String natives = (String)library.getNatives().get(os);
        if (natives != null)
          file = library.getArtifactPath(natives);
      }
      else {
        file = library.getArtifactPath();
      }

      if (file != null) {
    	  String url = library.hasExactUrl()? library.getExactDownloadUrl() : (library.getDownloadUrl() + file);
    	  if(url.startsWith("/")) url = source.getSelectedRepo() + url.substring(1);
    	  
    	  File local = new File(targetDirectory, "libraries/" + file);
    	  neededFiles.add(new Downloadable(url, local, force));
      }
    }

    return neededFiles;
  }
  
  public String getOriginalID(){
	  return this.original_id;
  }
  
  public void setOriginalID(String newid){
	  this.original_id = newid;
  }
  
  public boolean hasCustomUrl(){
	  return this.url != null;
  }
  
  public String getUrl(){
	  return this.url;
  }
  
  public void setUrl(String newurl){
	  this.url = newurl;
  }

  public String toString()
  {
    return "CompleteVersion{id='" + this.id + '\'' + ", time=" + this.time + ", type=" + this.type + ", libraries=" + this.libraries + ", mainClass='" + this.mainClass + '\'' + ", minimumLauncherVersion=" + this.minimumLauncherVersion + '}';
  }
  
  public String getJVMArguments()
  {
    return this.jvmArguments;
  }

  public String getMinecraftArguments()
  {
    return this.minecraftArguments;
  }
  
  public List<String> getUnnecessaryEntries(){
	  return this.unnecessaryEntries;
  }
  
  public int getTLauncherVersion(){
	  return this.tlauncherVersion;
  }
  
  public String getAssets(){
	  return (assets == null)? "legacy" : assets;
  }
  
  public void setId(String id){
	  if ((id == null) || (id.length() == 0)) throw new IllegalArgumentException("ID cannot be null or empty");
	  this.id = id;
  }

  public void setMinecraftArguments(String minecraftArguments) {
    if (minecraftArguments == null) throw new IllegalArgumentException("Process arguments cannot be null or empty");
    this.minecraftArguments = minecraftArguments;
  }
  
  public void setJVMArguments(String jvmArguments) {
	this.jvmArguments = jvmArguments;
  }

  public int getMinimumLauncherVersion() {
    return this.minimumLauncherVersion;
  }

  public void setMinimumLauncherVersion(int minimumLauncherVersion) {
    this.minimumLauncherVersion = minimumLauncherVersion;
  }

  public boolean appliesToCurrentEnvironment() {
    if (this.rules == null) return true;
    Rule.Action lastAction = Rule.Action.DISALLOW;

    for (Rule rule : this.rules) {
      Rule.Action action = rule.getAppliedAction();
      if (action != null) lastAction = action;
    }

    return lastAction == Rule.Action.ALLOW;
  }

  public String getIncompatibilityReason() {
    return this.incompatibilityReason;
  }
}