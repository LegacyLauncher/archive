package ru.turikhay.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.View;
import javax.xml.bind.DatatypeConverter;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.Images;

public class SwingUtil {
   private static final List favicons = new ArrayList();
   private static JLabel resizer;

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

   public static Dimension getPreferredSize(String html, boolean width, int prefSize) {
      if (resizer == null) {
         resizer = new JLabel();
      }

      resizer.setText(html);
      View view = (View)resizer.getClientProperty("html");
      view.setSize(width ? (float)prefSize : 0.0F, width ? 0.0F : (float)prefSize);
      float w = view.getPreferredSpan(0);
      float h = view.getPreferredSpan(1);
      return new Dimension((int)Math.ceil((double)w), (int)Math.ceil((double)h));
   }

   public static void setClipboard(String text) {
      if (text != null) {
         try {
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, new ClipboardOwner() {
               public void lostOwnership(Clipboard clipboard, Transferable contents) {
               }
            });
         } catch (Exception var3) {
            U.log("Could not copy", var3);
         }

      }
   }

   public static BufferedImage base64ToImage(String source) throws IOException {
      if (!source.startsWith("data:image/")) {
         throw new IllegalArgumentException("not a bse64 format");
      } else {
         int offset = "data:image/".length();
         if (!source.startsWith("png", offset) && !source.startsWith("jpg", offset) && !source.startsWith("gif")) {
            if (!source.startsWith("jpeg", offset)) {
               return null;
            }

            offset += 4;
         } else {
            offset += 3;
         }

         if (!source.substring(offset, offset + ";base64,".length()).equals(";base64,")) {
            return null;
         } else {
            offset += ";base64,".length();
            return ImageIO.read(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(source.substring(offset))));
         }
      }
   }

   public static Image loadImage(String source) throws IOException {
      try {
         return base64ToImage(source);
      } catch (IllegalArgumentException var2) {
         URL src = U.makeURL(source);
         return (Image)(src != null ? Images.loadMagnifiedImage(src) : Images.getImage(source, false));
      }
   }
}
