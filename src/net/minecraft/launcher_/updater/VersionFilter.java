package net.minecraft.launcher_.updater;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;

public class VersionFilter {
   private final Set types = new HashSet();

   public VersionFilter() {
      Collections.addAll(this.types, ReleaseType.values());
   }

   public Set getTypes() {
      return this.types;
   }

   public VersionFilter onlyForTypes(ReleaseType[] types) {
      this.types.clear();
      this.includeTypes(types);
      return this;
   }

   public VersionFilter onlyForType(ReleaseType type) {
      this.types.clear();
      this.includeType(type);
      return this;
   }

   public VersionFilter includeTypes(ReleaseType[] types) {
      if (types != null) {
         Collections.addAll(this.types, types);
      }

      return this;
   }

   public VersionFilter includeType(ReleaseType type) {
      this.types.add(type);
      return this;
   }

   public VersionFilter excludeTypes(ReleaseType[] types) {
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

   public VersionFilter excludeType(ReleaseType type) {
      this.types.remove(type);
      return this;
   }

   public boolean satisfies(Version v) {
      Iterator var3 = this.types.iterator();

      while(var3.hasNext()) {
         ReleaseType ct = (ReleaseType)var3.next();
         if (ct == v.getType()) {
            return true;
         }
      }

      return false;
   }
}
