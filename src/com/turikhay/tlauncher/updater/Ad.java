package com.turikhay.tlauncher.updater;

import com.turikhay.tlauncher.configuration.SimpleConfiguration;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.U;
import java.net.URL;

public class Ad {
   private static final String[] RANDOM_CHARS = new String[]{"creeper", "sheep", "skeleton", "steve", "wither", "zombie"};
   private final String content;
   private final int[] size;
   private final URL image;

   private Ad(SimpleConfiguration configuration) {
      this.content = configuration.get("ad.content");
      if (this.content == null) {
         throw new NullPointerException();
      } else {
         this.size = IntegerArray.toArray(configuration.get("ad.size"));
         if (this.size.length != 2) {
            throw new IllegalArgumentException("Invalid length of size array:" + this.size.length);
         } else {
            this.image = getInternal(configuration.get("ad.image"));
         }
      }
   }

   public String getContent() {
      return this.content;
   }

   public int[] getSize() {
      return this.size;
   }

   public URL getImage() {
      return this.image;
   }

   private static URL getInternal(String path) {
      if (path == null) {
         return null;
      } else {
         if (path.equals("random")) {
            path = (String)U.getRandom(RANDOM_CHARS) + ".png";
         }

         return ImageCache.getRes(path);
      }
   }

   static Ad parseFrom(SimpleConfiguration configuration) {
      try {
         return new Ad(configuration);
      } catch (RuntimeException var2) {
         return null;
      }
   }
}
