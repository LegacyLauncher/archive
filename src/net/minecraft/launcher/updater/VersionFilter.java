package net.minecraft.launcher.updater;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class VersionFilter
{
  private final Set<ReleaseType> types;
  private final Date oldMarker;

  public VersionFilter() {
	  this.types = new HashSet<ReleaseType>();
	  Collections.addAll(this.types, ReleaseType.values());
    
	  GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	  calendar.set(2013, 03, 20, 15, 00); // Versions before 1.5.2
	  
	  oldMarker = calendar.getTime();
  }

  public Set<ReleaseType> getTypes() {
    return this.types;
  }

  public VersionFilter onlyForTypes(ReleaseType[] types) {
    this.types.clear();
    includeTypes(types);
    return this;
  }
  public VersionFilter onlyForType(ReleaseType type){
	  this.types.clear();
	  includeType(type);
	  return this;
  }

  public VersionFilter includeTypes(ReleaseType[] types) {
    if (types != null) Collections.addAll(this.types, types);
    return this;
  }
  
  public VersionFilter includeType(ReleaseType type){
	  this.types.add(type);
	  return this;
  }

  public VersionFilter excludeTypes(ReleaseType[] types) {
    if (types != null) {
      for (ReleaseType type : types) {
        this.types.remove(type);
      }
    }
    return this;
  }
  
  public VersionFilter excludeType(ReleaseType type) {
	this.types.remove(type);
	return this;
  }
  
  public boolean satisfies(Version v){
	  if(v.getReleaseType() == null)
		  return true;
	  
	  Date releaseTime = v.getReleaseTime();
	  
	  boolean old = !v.getReleaseType().isOld() // Not marked as old already
			  && releaseTime != null // Valid release time
			  && releaseTime.before(oldMarker) // Is old
			  && releaseTime.getTime() > 0; // Manually installed Forge versions may be parsed as old.
			  
	  if(old) return types.contains(ReleaseType.OLD);
	  
	  for(ReleaseType ct : types)
		  if(ct == v.getReleaseType())
			  return true;
	  
	  return false;
  }
  
  public String toString(){
	  return "VersionFilter" + this.types;
  }
}