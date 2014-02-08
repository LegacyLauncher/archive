package net.minecraft.launcher.versions;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseType {
   SNAPSHOT("snapshot"),
   RELEASE("release", true),
   MODIFIED("modified"),
   OLD("old"),
   OLD_BETA("old-beta"),
   OLD_ALPHA("old-alpha"),
   UNKNOWN("unknown");

   private static final Map lookup = new HashMap();
   private final String name;
   private final boolean desired;

   static {
      ReleaseType[] var3;
      int var2 = (var3 = values()).length;

      for(int var1 = 0; var1 < var2; ++var1) {
         ReleaseType type = var3[var1];
         lookup.put(type.getName(), type);
      }

   }

   private ReleaseType(String name, boolean desired) {
      this.name = name;
      this.desired = desired;
   }

   private ReleaseType(String name) {
      this(name, false);
   }

   public String getName() {
      return this.name;
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
