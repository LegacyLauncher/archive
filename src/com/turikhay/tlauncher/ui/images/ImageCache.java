package com.turikhay.tlauncher.ui.images;

import com.turikhay.tlauncher.exceptions.TLauncherException;
import java.awt.Image;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ImageCache {
   private static final Map imageCache = Collections.synchronizedMap(new HashMap());

   public static Image loadImage(URL url, boolean throwIfError) {
      if (url == null) {
         throw new NullPointerException("URL is NULL");
      } else {
         try {
            Image image = ImageIO.read(url);
            imageCache.put(url, image);
            return image;
         } catch (Exception var3) {
            if (throwIfError) {
               throw new TLauncherException("Cannot load required image: " + url, var3);
            } else {
               var3.printStackTrace();
               return null;
            }
         }
      }
   }

   public static Image loadImage(URL url) {
      return loadImage(url, true);
   }

   public static Image getImage(String uri, boolean throwIfError) {
      return loadImage(getRes(uri), throwIfError);
   }

   public static Image getImage(String uri) {
      return getImage(uri, true);
   }

   public static URL getRes(String uri) {
      return uri == null ? null : ImageCache.class.getResource(uri);
   }
}
