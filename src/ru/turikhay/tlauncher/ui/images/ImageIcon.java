package ru.turikhay.tlauncher.ui.images;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import javax.swing.Icon;

public class ImageIcon implements Icon {
   private transient Image image;
   private int width;
   private int height;

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
}
