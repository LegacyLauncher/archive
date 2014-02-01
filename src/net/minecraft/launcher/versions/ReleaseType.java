package net.minecraft.launcher.versions;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseType {
   SNAPSHOT("snapshot", "Enable experimental development versions (\"snapshots\")", false),
   RELEASE("release", (String)null, true),
   MODIFIED("modified", (String)null, false),
   CHEAT("cheat", (String)null, false),
   OLD("old", (String)null, false),
   OLD_BETA("old-beta", "Allow use of old \"Beta\" minecraft versions (From 2010-2011)", false),
   OLD_ALPHA("old-alpha", "Allow use of old \"Alpha\" minecraft versions (From 2010)", false);

   private static final Map lookup = new HashMap();
   private final String name;
   private final String description;
   private final boolean desired;

   static {
      ReleaseType[] var3;
      int var2 = (var3 = values()).length;

      for(int var1 = 0; var1 < var2; ++var1) {
         ReleaseType type = var3[var1];
         lookup.put(type.getName(), type);
      }

   }

   private ReleaseType(String name, String description, boolean desired) {
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

   public boolean isDesired() {
      return this.desired;
   }

   public boolean isOld() {
      return this.name.startsWith("old");
   }

   public static ReleaseType getByName(String name) {
      return (ReleaseType)lookup.get(name);
   }
}
