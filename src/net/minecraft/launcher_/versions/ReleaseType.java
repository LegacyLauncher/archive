package net.minecraft.launcher_.versions;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseType
{
	SNAPSHOT("snapshot", "Enable experimental development versions (\"snapshots\")"), 
	RELEASE("release", null), 
	OLD_BETA("old-beta", "Allow use of old \"Beta\" minecraft versions (From 2010-2011)"), 
	OLD_ALPHA("old-alpha", "Allow use of old \"Alpha\" minecraft versions (From 2010)");

  private static final Map<String, ReleaseType> lookup;
  private final String name, description;

  private ReleaseType(String name, String description)
  {
    this.name = name;
    this.description = description;
  }

  public String getName() {
	  return this.name;
  }
  
  public String getDescription() {
	  return this.description;
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
