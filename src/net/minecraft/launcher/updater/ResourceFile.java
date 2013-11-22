package net.minecraft.launcher.updater;

public class ResourceFile {
   public final String path;
   public final String md5;

   public ResourceFile(String path, String md5) {
      this.path = path;
      this.md5 = md5;
   }

   public String toString() {
      return this.path + "\t" + this.md5;
   }
}
