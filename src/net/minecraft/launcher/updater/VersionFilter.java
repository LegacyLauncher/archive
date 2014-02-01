package net.minecraft.launcher.updater;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class VersionFilter {
   private final Set types = new HashSet();
   private final Date oldMarker;

   public VersionFilter() {
      Collections.addAll(this.types, ReleaseType.values());
      GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      calendar.set(2013, 3, 20, 15, 0);
      this.oldMarker = calendar.getTime();
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
      if (v.getType() == null) {
         return true;
      } else {
         boolean old = v.getReleaseTime().before(this.oldMarker) && !v.getType().isOld() && v.getReleaseTime().getTime() > 0L;
         if (old) {
            return this.types.contains(ReleaseType.OLD);
         } else {
            Iterator var4 = this.types.iterator();

            while(var4.hasNext()) {
               ReleaseType ct = (ReleaseType)var4.next();
               if (ct == v.getType()) {
                  return true;
               }
            }

            return false;
         }
      }
   }
}
