package ru.turikhay.util;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import ru.turikhay.tlauncher.ui.images.ImageCache;

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
            Image image = ImageCache.getImage("fav" + i + ".png", false);
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

   public static void initLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var1) {
         log("Can't set system look and feel.");
         var1.printStackTrace();
      }

   }

   public static void initFontSize(int defSize) {
      try {
         UIDefaults defaults = UIManager.getDefaults();
         int minSize = defSize;
         int maxSize = defSize + 2;
         Enumeration e = defaults.keys();

         while(e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font) {
               Font font = (Font)value;
               int size = font.getSize();
               if (size < minSize) {
                  size = minSize;
               } else if (size > maxSize) {
                  size = maxSize;
               }

               if (value instanceof FontUIResource) {
                  defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), size));
               } else {
                  defaults.put(key, new Font(font.getName(), font.getStyle(), size));
               }
            }
         }
      } catch (Exception var9) {
         log("Cannot change font sizes!", var9);
      }

   }

   public static Cursor getCursor(int type) {
      try {
         return Cursor.getPredefinedCursor(type);
      } catch (IllegalArgumentException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static void setFontSize(JComponent comp, float size) {
      comp.setFont(comp.getFont().deriveFont(size));
   }

   private static void log(Object... o) {
      U.log("[Swing]", o);
   }
}
