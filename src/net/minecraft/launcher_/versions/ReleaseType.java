package net.minecraft.launcher_.versions;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseType {
   SNAPSHOT("snapshot", "Enable experimental development versions (\"snapshots\")"),
   RELEASE("release", (String)null),
   CHEAT("cheat", (String)null),
   OLD_BETA("old-beta", "Allow use of old \"Beta\" minecraft versions (From 2010-2011)"),
   OLD_ALPHA("old-alpha", "Allow use of old \"Alpha\" minecraft versions (From 2010)");

   private static final Map lookup = new HashMap();
   private final String name;
   private final String description;

   static {
      ReleaseType[] var3;
      int var2 = (var3 = values()).length;

      for(int var1 = 0; var1 < var2; ++var1) {
         ReleaseType type = var3[var1];
         lookup.put(type.getName(), type);
      }

   }

   private ReleaseType(String name, String description) {
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
      return (ReleaseType)lookup.get(name);
   }
}
