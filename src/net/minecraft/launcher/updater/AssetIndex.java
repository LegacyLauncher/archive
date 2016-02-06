package net.minecraft.launcher.updater;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ru.turikhay.util.FileUtil;

public class AssetIndex {
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

   public static String getPath(String hash) {
      return hash.substring(0, 2) + "/" + hash;
   }

   public static String getHash(File file) {
      return FileUtil.getDigest(file, "SHA", 40);
   }

   public class AssetObject {
      private String hash;
      private long size;
      private boolean reconstruct;
      private String compressedHash;
      private long compressedSize;

      public String hash() {
         return this.isCompressed() ? this.compressedHash : this.hash;
      }

      public long size() {
         return this.isCompressed() ? this.compressedSize : this.size;
      }

      public String getHash() {
         return this.hash;
      }

      public boolean isCompressed() {
         return this.compressedHash != null;
      }

      public String getCompressedHash() {
         return this.compressedHash;
      }

      public long getCompressedSize() {
         return this.compressedSize;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AssetIndex.AssetObject that = (AssetIndex.AssetObject)o;
            if (this.compressedSize != that.compressedSize) {
               return false;
            } else if (this.reconstruct != that.reconstruct) {
               return false;
            } else if (this.size != that.size) {
               return false;
            } else {
               if (this.compressedHash != null) {
                  if (!this.compressedHash.equals(that.compressedHash)) {
                     return false;
                  }
               } else if (that.compressedHash != null) {
                  return false;
               }

               return this.hash != null ? this.hash.equals(that.hash) : that.hash == null;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.hash != null ? this.hash.hashCode() : 0;
         result = 31 * result + (int)(this.size ^ this.size >>> 32);
         result = 31 * result + (this.reconstruct ? 1 : 0);
         result = 31 * result + (this.compressedHash != null ? this.compressedHash.hashCode() : 0);
         result = 31 * result + (int)(this.compressedSize ^ this.compressedSize >>> 32);
         return result;
      }
   }
}
