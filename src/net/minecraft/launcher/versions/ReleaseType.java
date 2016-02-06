package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import ru.turikhay.tlauncher.repository.Repository;

public enum ReleaseType {
   RELEASE("release", false, true),
   SNAPSHOT("snapshot", true, true),
   MODIFIED("modified", true, true),
   OLD_BETA("old-beta", true, true),
   OLD_ALPHA("old-alpha", true, true),
   UNKNOWN("unknown", false, false);

   private static final Map lookup;
   private static final List defaultTypes;
   private static final List definableTypes;
   private final String name;
   private final boolean isDefinable;
   private final boolean isDefault;

   private ReleaseType(String name, boolean isDefinable, boolean isDefault) {
      this.name = name;
      this.isDefinable = isDefinable;
      this.isDefault = isDefault;
   }

   String getName() {
      return this.name;
   }

   public boolean isDefault() {
      return this.isDefault;
   }

   public boolean isDefinable() {
      return this.isDefinable;
   }

   public String toString() {
      return super.toString().toLowerCase();
   }

   public static Collection valuesCollection() {
      return lookup.values();
   }

   public static List getDefault() {
      return defaultTypes;
   }

   public static List getDefinable() {
      return definableTypes;
   }

   static {
      HashMap types = new HashMap(values().length);
      ArrayList deflTypes = new ArrayList();
      ArrayList defnTypes = new ArrayList();
      ReleaseType[] var6;
      int var5 = (var6 = values()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         ReleaseType type = var6[var4];
         types.put(type.getName(), type);
         if (type.isDefault()) {
            deflTypes.add(type);
         }

         if (type.isDefinable()) {
            defnTypes.add(type);
         }
      }

      lookup = Collections.unmodifiableMap(types);
      defaultTypes = Collections.unmodifiableList(deflTypes);
      definableTypes = Collections.unmodifiableList(defnTypes);
   }

   public static enum SubType {
      OLD_RELEASE("old_release") {
         private final Date marker;

         {
            GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            calendar.set(2013, 3, 20, 15, 0);
            this.marker = calendar.getTime();
         }

         public boolean isSubType(Version version) {
            return !version.getReleaseType().toString().startsWith("old") && version.getReleaseTime().getTime() >= 0L && version.getReleaseTime().before(this.marker);
         }
      },
      REMOTE("remote") {
         public boolean isSubType(Version version) {
            return version.getSource() != Repository.LOCAL_VERSION_REPO;
         }
      };

      private static final Map lookup;
      private static final List defaultSubTypes;
      private final String name;
      private final boolean isDefault;

      private SubType(String name, boolean isDefault) {
         this.name = name;
         this.isDefault = isDefault;
      }

      private SubType(String name) {
         this(name, true);
      }

      public String getName() {
         return this.name;
      }

      public boolean isDefault() {
         return this.isDefault;
      }

      public String toString() {
         return super.toString().toLowerCase();
      }

      public static Collection valuesCollection() {
         return lookup.values();
      }

      public static List getDefault() {
         return defaultSubTypes;
      }

      public static List get(Version version) {
         ArrayList result = new ArrayList();
         ReleaseType.SubType[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            ReleaseType.SubType subType = var4[var2];
            if (subType.isSubType(version)) {
               result.add(subType);
            }
         }

         return result;
      }

      public abstract boolean isSubType(Version var1);

      // $FF: synthetic method
      SubType(String x2, Object x3) {
         this(x2);
      }

      static {
         HashMap subTypes = new HashMap(values().length);
         ArrayList defSubTypes = new ArrayList();
         ReleaseType.SubType[] var5;
         int var4 = (var5 = values()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            ReleaseType.SubType subType = var5[var3];
            subTypes.put(subType.getName(), subType);
            if (subType.isDefault()) {
               defSubTypes.add(subType);
            }
         }

         lookup = Collections.unmodifiableMap(subTypes);
         defaultSubTypes = Collections.unmodifiableList(defSubTypes);
      }
   }
}
