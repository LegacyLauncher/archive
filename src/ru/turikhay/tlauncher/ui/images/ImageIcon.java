package ru.turikhay.tlauncher.ui.images;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import javax.swing.Icon;
import javax.swing.JLabel;

public class ImageIcon implements Icon {
   private transient Image image;
   private int width;
   private int height;
   private ImageIcon.DisabledImageIcon disabledInstance;

   public ImageIcon(Image image, int width, int height) {
      this.setImage(image, width, height);
   }

   public ImageIcon(Image image) {
      this(image, 0, 0);
   }

   public void setImage(Image image, int preferredWidth, int preferredHeight) {
      if (image == null) {
         this.image = null;
      } else {
         int realWidth = image.getWidth((ImageObserver)null);
         int realHeight = image.getHeight((ImageObserver)null);
         this.width = preferredWidth > 0 ? preferredWidth : realWidth;
         this.height = preferredHeight > 0 ? preferredHeight : realHeight;
         Image scaled = image.getScaledInstance(this.width, this.height, 4);
         this.image = scaled;
      }
   }

   public void paintIcon(Component c, Graphics g, int x, int y) {
      if (this.image != null) {
         g.drawImage(this.image, x, y, this.width, this.height, (ImageObserver)null);
      }
   }

   public int getIconWidth() {
      return this.width;
   }

   public int getIconHeight() {
      return this.height;
   }

   public ImageIcon.DisabledImageIcon getDisabledInstance() {
      if (this.disabledInstance == null) {
         this.disabledInstance = new ImageIcon.DisabledImageIcon((ImageIcon.DisabledImageIcon)null);
      }

      return this.disabledInstance;
   }

   public static ImageIcon setup(JLabel label, ImageIcon icon) {
      if (label == null) {
         return null;
      } else {
         label.setIcon(icon);
         if (icon != null) {
            label.setDisabledIcon(icon.getDisabledInstance());
         }

         return icon;
      }
   }

   public static ImageIcon get(Icon icon) {
      if (icon == null) {
         return null;
      } else if (icon instanceof ImageIcon) {
         return (ImageIcon)icon;
      } else {
         return icon instanceof ImageIcon.DisabledImageIcon ? ((ImageIcon.DisabledImageIcon)icon).getParent() : null;
      }
   }

   public class DisabledImageIcon implements Icon {
      private float disabledOpacity;
      private AlphaComposite opacityComposite;

      private DisabledImageIcon() {
         this.setDisabledOpacity(0.5F);
      }

      public final ImageIcon getParent() {
         return ImageIcon.this;
      }

      public float getDisabledOpacity() {
         return this.disabledOpacity;
      }

      public void setDisabledOpacity(float f) {
         this.disabledOpacity = f;
         this.opacityComposite = AlphaComposite.getInstance(3, this.disabledOpacity);
      }

      public void paintIcon(Component c, Graphics g0, int x, int y) {
         if (ImageIcon.this.image != null) {
            Graphics2D g = (Graphics2D)g0;
            Composite oldComposite = g.getComposite();
            g.setComposite(this.opacityComposite);
            g.drawImage(ImageIcon.this.image, x, y, ImageIcon.this.width, ImageIcon.this.height, (ImageObserver)null);
            g.setComposite(oldComposite);
         }
      }

      public int getIconWidth() {
         return ImageIcon.this.getIconWidth();
      }

      public int getIconHeight() {
         return ImageIcon.this.getIconHeight();
      }

      // $FF: synthetic method
      DisabledImageIcon(ImageIcon.DisabledImageIcon var2) {
         this();
      }
   }
}
