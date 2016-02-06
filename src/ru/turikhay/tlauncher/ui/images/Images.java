package ru.turikhay.tlauncher.ui.images;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import javax.imageio.ImageIO;
import ru.turikhay.tlauncher.exceptions.TLauncherException;
import ru.turikhay.util.SwingUtil;

public class Images {
   private static final Map loadedImages = new Hashtable();
   private static final Map magnifiedImages = new Hashtable();

   public static BufferedImage loadImage(URL url, boolean throwIfError) {
      if (url == null) {
         throw new NullPointerException("URL is NULL");
      } else {
         WeakReference ref = (WeakReference)loadedImages.get(url);
         BufferedImage image = ref == null ? null : (BufferedImage)ref.get();
         if (image != null) {
            return image;
         } else {
            try {
               image = ImageIO.read(url);
            } catch (Exception var5) {
               if (throwIfError) {
                  throw new TLauncherException("Cannot load required image: " + url, var5);
               }

               var5.printStackTrace();
               return null;
            }

            loadedImages.put(url, new WeakReference(image));
            return image;
         }
      }
   }

   public static Image loadMagnifiedImage(URL url, boolean throwIfError) {
      WeakReference ref = (WeakReference)magnifiedImages.get(url);
      Image cached = ref == null ? null : (Image)ref.get();
      if (cached != null) {
         return cached;
      } else {
         BufferedImage image = loadImage(url, throwIfError);
         Image scaled = image.getScaledInstance(SwingUtil.magnify(image.getWidth()), SwingUtil.magnify(image.getHeight()), 4);
         magnifiedImages.put(url, new WeakReference(scaled));
         return scaled;
      }
   }

   public static Image loadMagnifiedImage(URL url) {
      return loadMagnifiedImage(url, true);
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

   public static ImageIcon getIcon(String uri) {
      return getIcon(uri, 0, 0);
   }

   public static ImageIcon getScaledIcon(String uri, int widthNheight) {
      return getIcon(uri, SwingUtil.magnify(widthNheight));
   }

   public static URL getRes(String uri, boolean throwIfNull) {
      if (uri == null) {
         if (throwIfNull) {
            throw new NullPointerException();
         } else {
            return null;
         }
      } else {
         URL url = Images.class.getResource(uri);
         if (url == null && throwIfNull) {
            throw new RuntimeException("cannot find resource: \"" + uri + "\"");
         } else {
            return url;
         }
      }
   }

   public static URL getRes(String uri) {
      return getRes(uri, true);
   }
}
