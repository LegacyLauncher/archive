package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ReleaseType {
   RELEASE("release", false, true),
   SNAPSHOT("snapshot"),
   OLD_BETA("old-beta"),
   OLD_ALPHA("old-alpha"),
   OLD("old"),
   MODIFIED("modified"),
   UNKNOWN("unknown", false, false);

   private static final Map lookup = new HashMap();
   private static ReleaseType[] defaultTypes;
   private final String name;
   private final boolean isDefault;
   private final boolean isDesired;

   static {
      ReleaseType[] var3;
      int var2 = (var3 = values()).length;

      for(int var1 = 0; var1 < var2; ++var1) {
         ReleaseType type = var3[var1];
         lookup.put(type.getName(), type);
      }

   }

   private ReleaseType(String name, boolean isDefault, boolean isDesired) {
      this.name = name;
      this.isDefault = isDefault;
      this.isDesired = isDesired;
   }

   private ReleaseType(String name, boolean isDesired) {
      this(name, true, isDesired);
   }

   private ReleaseType(String name) {
      this(name, true, false);
   }

   public String getName() {
      return this.name;
   }

   public boolean isDesired() {
      return this.isDesired;
   }

   public boolean isDefault() {
      return this.isDefault;
   }

   public boolean isOld() {
      return this.name.startsWith("old");
   }

   public String toString() {
      return super.toString().toLowerCase();
   }

   public static ReleaseType getByName(String name) {
      return (ReleaseType)lookup.get(name);
   }

   public static ReleaseType[] getDefinable() {
      if (defaultTypes != null) {
         return defaultTypes;
      } else {
         List types = new ArrayList();
         ReleaseType[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            ReleaseType type = var4[var2];
            if (type.isDefault) {
               types.add(type);
            }
         }

         ReleaseType[] r = new ReleaseType[types.size()];
         types.toArray(r);
         defaultTypes = r;
         return r;
      }
   }
}
