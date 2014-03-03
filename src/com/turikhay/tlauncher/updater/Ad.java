package com.turikhay.tlauncher.updater;

import com.turikhay.tlauncher.configuration.SimpleConfiguration;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.U;
import java.net.URL;

public class Ad {
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
      return path == null ? null : ImageCache.getRes(path);
   }

   static Ad parseFrom(SimpleConfiguration configuration) {
      try {
         return new Ad(configuration);
      } catch (RuntimeException var2) {
         U.log(var2);
         return null;
      }
   }
}
