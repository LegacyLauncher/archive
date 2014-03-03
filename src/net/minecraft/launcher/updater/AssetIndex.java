package net.minecraft.launcher.updater;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AssetIndex {
   public static final String DEFAULT_ASSET_NAME = "legacy";
   private Map objects = new LinkedHashMap();
   private boolean virtual;

   public Map getFileMap() {
      return this.objects;
   }

   public Set getUniqueObjects() {
      return new HashSet(this.objects.values());
   }

   public boolean isVirtual() {
      return this.virtual;
   }

   public class AssetObject {
      private String filename;
      private String hash;
      private long size;

      public String getHash() {
         return this.hash;
      }

      public long getSize() {
         return this.size;
      }

      public String getFilename() {
         if (this.filename == null) {
            this.filename = this.getHash().substring(0, 2) + "/" + this.getHash();
         }

         return this.filename;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AssetIndex.AssetObject that = (AssetIndex.AssetObject)o;
            return this.size != that.size ? false : this.hash.equals(that.hash);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.hash.hashCode();
         result = 31 * result + (int)(this.size ^ this.size >>> 32);
         return result;
      }
   }
}
