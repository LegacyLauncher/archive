package ru.turikhay.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.Images;

public class SwingUtil {
   private static final List favicons = new ArrayList();

   public static List getFavicons() {
      if (!favicons.isEmpty()) {
         return Collections.unmodifiableList(favicons);
      } else {
         int[] sizes = new int[]{256, 128, 96, 64, 48, 32, 24, 16};
         String loaded = "";
         int[] var5 = sizes;
         int var4 = sizes.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            int i = var5[var3];
            Image image = Images.getImage("fav" + i + ".png", false);
            if (image != null) {
               loaded = loaded + ", " + i + "px";
               favicons.add(image);
            }
         }

         if (loaded.isEmpty()) {
            log("No favicon is loaded.");
         } else {
            log("Favicons loaded:", loaded.substring(2));
         }

         return favicons;
      }
   }

   public static void setFavicons(JFrame frame) {
      frame.setIconImages(getFavicons());
   }

   public static boolean initLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         return true;
      } catch (Exception var1) {
         log("Can't set system look and feel.", var1);
         return false;
      }
   }

   public static void resetLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception var1) {
         log("Can't set default look and feel!", var1);
      }

   }

   public static void initFontSize(int defSize) {
      try {
         UIDefaults e = UIManager.getDefaults();
         int minSize = defSize;
         int maxSize = defSize + 2;
         Enumeration e1 = e.keys();

         while(e1.hasMoreElements()) {
            Object key = e1.nextElement();
            Object value = e.get(key);
            if (value instanceof Font) {
               Font font = (Font)value;
               int size = font.getSize();
               if (size < minSize) {
                  size = minSize;
               } else if (size > maxSize) {
                  size = maxSize;
               }

               if (value instanceof FontUIResource) {
                  e.put(key, new FontUIResource(font.getName(), font.getStyle(), size));
               } else {
                  e.put(key, new Font(font.getName(), font.getStyle(), size));
               }
            }
         }
      } catch (Exception var9) {
         log("Cannot change font sizes!", var9);
      }

   }

   public static int magnify(int i) {
      return (int)Math.rint((double)i * TLauncherFrame.magnifyDimensions);
   }

   public static float magnify(float i) {
      return (float)((double)i * TLauncherFrame.magnifyDimensions);
   }

   public static Dimension magnify(Dimension d) {
      return new Dimension(magnify(d.width), magnify(d.height));
   }

   public static Insets magnify(Insets i) {
      i.top = magnify(i.top);
      i.left = magnify(i.left);
      i.bottom = magnify(i.bottom);
      i.right = magnify(i.right);
      return i;
   }

   private static void log(Object... o) {
      U.log("[Swing]", o);
   }
}
