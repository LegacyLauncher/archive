package net.minecraft.launcher.versions;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseType
{
	SNAPSHOT("snapshot", "Enable experimental development versions (\"snapshots\")", false), 
	RELEASE("release", null, true),
	CHEAT("cheat", null, false),
	OLD_BETA("old-beta", "Allow use of old \"Beta\" minecraft versions (From 2010-2011)", false), 
	OLD_ALPHA("old-alpha", "Allow use of old \"Alpha\" minecraft versions (From 2010)", false);

  private static final Map<String, ReleaseType> lookup;
  private final String name, description;
  private final boolean desired;

  private ReleaseType(String name, String description, boolean desired)
  {
    this.name = name;
    this.description = description;
    this.desired = desired;
  }

  public String getName() {
	  return this.name;
  }
  
  public String getDescription() {
	  return this.description;
  }
  
  public boolean isDesired(){
	  return desired;
  }

  public static ReleaseType getByName(String name) {
    return (ReleaseType) lookup.get(name);
  }

  static
  {
    lookup = new HashMap<String, ReleaseType>();

    for (ReleaseType type : values())
      lookup.put(type.getName(), type);
  }
}
