package ru.turikhay.tlauncher.ui.images;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import ru.turikhay.tlauncher.exceptions.TLauncherException;

public class ImageCache {
   private static final boolean THROW_IF_ERROR = true;
   private static final Map imageCache = Collections.synchronizedMap(new HashMap());

   public static BufferedImage loadImage(URL url, boolean throwIfError) {
      if (url == null) {
         throw new NullPointerException("URL is NULL");
      } else {
         try {
            BufferedImage image = ImageIO.read(url);
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

   public static BufferedImage loadImage(URL url) {
      return loadImage(url, true);
   }

   public static BufferedImage getImage(String uri, boolean throwIfError) {
      return loadImage(getRes(uri), throwIfError);
   }

   public static BufferedImage getImage(String uri) {
      return getImage(uri, true);
   }

   public static ImageIcon getIcon(String uri, int width, int height, boolean throwIfError) {
      return new ImageIcon(getImage(uri, throwIfError), width, height);
   }

   public static ImageIcon getIcon(String uri, int width, int height) {
      return getIcon(uri, width, height, true);
   }

   public static ImageIcon getIcon(String uri, int widthNheight) {
      return getIcon(uri, widthNheight, widthNheight, true);
   }

   public static ImageIcon getIcon(String uri, boolean throwIfError) {
      return new ImageIcon(getImage(uri, throwIfError));
   }

   public static ImageIcon getIcon(String uri) {
      return getIcon(uri, 0, 0);
   }

   public static URL getRes(String uri) {
      return uri == null ? null : ImageCache.class.getResource(uri);
   }
}
