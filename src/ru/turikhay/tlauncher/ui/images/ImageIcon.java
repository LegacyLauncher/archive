package ru.turikhay.tlauncher.ui.images;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import javax.swing.JLabel;

public class ImageIcon implements ExtendedIcon {
   private transient Image image;
   private int width;
   private int height;
   private DisabledImageIcon disabledInstance;

   public ImageIcon(Image image, int width, int height) {
      this.setImage(image, width, height);
   }

   public void setImage(Image image, int preferredWidth, int preferredHeight) {
      if (image == null) {
         this.image = null;
      } else {
         this.image = image;
         this.setIconSize(preferredWidth, preferredHeight);
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

   public void setIconSize(int width, int height) {
      this.width = width > 0 ? width : (this.image == null ? 0 : this.image.getWidth((ImageObserver)null));
      this.height = height > 0 ? height : (this.image == null ? 0 : this.image.getHeight((ImageObserver)null));
      if (this.image != null) {
         this.image = this.image.getScaledInstance(this.width, this.height, 4);
      }

   }

   public DisabledImageIcon getDisabledInstance() {
      if (this.disabledInstance == null) {
         this.disabledInstance = new DisabledImageIcon(this);
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
}
