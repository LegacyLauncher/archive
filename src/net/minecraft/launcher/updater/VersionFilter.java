package net.minecraft.launcher.updater;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class VersionFilter {
   private final Set types = new HashSet(ReleaseType.valuesCollection());
   private final Set subTypes = new HashSet(ReleaseType.SubType.valuesCollection());

   public Set getTypes() {
      return this.types;
   }

   public Set getSubTypes() {
      return this.subTypes;
   }

   public VersionFilter onlyForType(ReleaseType... types) {
      this.types.clear();
      this.include(types);
      return this;
   }

   public VersionFilter onlyForType(ReleaseType.SubType... subTypes) {
      this.subTypes.clear();
      this.include(subTypes);
      return this;
   }

   public VersionFilter include(ReleaseType... types) {
      if (types != null) {
         Collections.addAll(this.types, types);
      }

      return this;
   }

   public VersionFilter include(ReleaseType.SubType... types) {
      if (types != null) {
         Collections.addAll(this.subTypes, types);
      }

      return this;
   }

   public VersionFilter exclude(ReleaseType... types) {
      if (types != null) {
         ReleaseType[] var5 = types;
         int var4 = types.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            ReleaseType type = var5[var3];
            this.types.remove(type);
         }
      }

      return this;
   }

   public VersionFilter exclude(ReleaseType.SubType... types) {
      if (types != null) {
         ReleaseType.SubType[] var5 = types;
         int var4 = types.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            ReleaseType.SubType type = var5[var3];
            this.subTypes.remove(type);
         }
      }

      return this;
   }

   public boolean satisfies(Version v) {
      ReleaseType releaseType = v.getReleaseType();
      if (releaseType == null) {
         return true;
      } else if (!this.types.contains(releaseType)) {
         return false;
      } else {
         List subTypeList = ReleaseType.SubType.get(v);
         Iterator i$ = subTypeList.iterator();

         ReleaseType.SubType subType;
         do {
            if (!i$.hasNext()) {
               return true;
            }

            subType = (ReleaseType.SubType)i$.next();
         } while(this.subTypes.contains(subType));

         return false;
      }
   }

   public String toString() {
      return "VersionFilter" + this.types;
   }
}
