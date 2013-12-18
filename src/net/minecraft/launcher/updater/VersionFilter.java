package net.minecraft.launcher.updater;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class VersionFilter
{
  private final Set<ReleaseType> types = new HashSet<ReleaseType>();

  public VersionFilter() {
    Collections.addAll(this.types, ReleaseType.values());
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
	  for(ReleaseType ct : types) if(ct == v.getType()) return true;
	  return false;
  }
}