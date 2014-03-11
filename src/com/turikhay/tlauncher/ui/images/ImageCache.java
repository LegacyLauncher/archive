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

   public static Image getImage(String uri) {
      return getImage(uri, true);
   }

   public static Image getImage(String uri, boolean throwIfError) {
      if (uri == null) {
         throw new NullPointerException("URL is NULL");
      } else if (imageCache.containsKey(uri)) {
         return (Image)imageCache.get(uri);
      } else {
         try {
            Image image = ImageIO.read(getRes(uri));
            imageCache.put(uri, image);
            return image;
         } catch (Exception var3) {
            if (throwIfError) {
               throw new TLauncherException("Cannot load required image: " + uri, var3);
            } else {
               var3.printStackTrace();
               return null;
            }
         }
      }
   }

   public static URL getRes(String uri) {
      return ImageCache.class.getResource(uri);
   }
}
