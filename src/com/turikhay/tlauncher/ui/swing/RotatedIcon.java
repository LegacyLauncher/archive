package com.turikhay.tlauncher.ui.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;

public class RotatedIcon implements Icon {
   private Icon icon;
   private RotatedIcon.Rotate rotate;
   private double angle;

   public RotatedIcon(Icon icon) {
      this(icon, RotatedIcon.Rotate.UP);
   }

   public RotatedIcon(Icon icon, RotatedIcon.Rotate rotate) {
      this.icon = icon;
      this.rotate = rotate;
   }

   public RotatedIcon(Icon icon, double angle) {
      this(icon, RotatedIcon.Rotate.ABOUT_CENTER);
      this.angle = angle;
   }

   public Icon getIcon() {
      return this.icon;
   }

   public RotatedIcon.Rotate getRotate() {
      return this.rotate;
   }

   public double getAngle() {
      return this.angle;
   }

   public int getIconWidth() {
      if (this.rotate == RotatedIcon.Rotate.ABOUT_CENTER) {
         double radians = Math.toRadians(this.angle);
         double sin = Math.abs(Math.sin(radians));
         double cos = Math.abs(Math.cos(radians));
         int width = (int)Math.floor((double)this.icon.getIconWidth() * cos + (double)this.icon.getIconHeight() * sin);
         return width;
      } else {
         return this.rotate == RotatedIcon.Rotate.UPSIDE_DOWN ? this.icon.getIconWidth() : this.icon.getIconHeight();
      }
   }

   public int getIconHeight() {
      if (this.rotate == RotatedIcon.Rotate.ABOUT_CENTER) {
         double radians = Math.toRadians(this.angle);
         double sin = Math.abs(Math.sin(radians));
         double cos = Math.abs(Math.cos(radians));
         int height = (int)Math.floor((double)this.icon.getIconHeight() * cos + (double)this.icon.getIconWidth() * sin);
         return height;
      } else {
         return this.rotate == RotatedIcon.Rotate.UPSIDE_DOWN ? this.icon.getIconHeight() : this.icon.getIconWidth();
      }
   }

   public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D)g.create();
      int cWidth = this.icon.getIconWidth() / 2;
      int cHeight = this.icon.getIconHeight() / 2;
      int xAdjustment = this.icon.getIconWidth() % 2 == 0 ? 0 : -1;
      int yAdjustment = this.icon.getIconHeight() % 2 == 0 ? 0 : -1;
      if (this.rotate == RotatedIcon.Rotate.DOWN) {
         g2.translate(x + cHeight, y + cWidth);
         g2.rotate(Math.toRadians(90.0D));
         this.icon.paintIcon(c, g2, -cWidth, yAdjustment - cHeight);
      } else if (this.rotate == RotatedIcon.Rotate.UP) {
         g2.translate(x + cHeight, y + cWidth);
         g2.rotate(Math.toRadians(-90.0D));
         this.icon.paintIcon(c, g2, xAdjustment - cWidth, -cHeight);
      } else if (this.rotate == RotatedIcon.Rotate.UPSIDE_DOWN) {
         g2.translate(x + cWidth, y + cHeight);
         g2.rotate(Math.toRadians(180.0D));
         this.icon.paintIcon(c, g2, xAdjustment - cWidth, yAdjustment - cHeight);
      } else if (this.rotate == RotatedIcon.Rotate.ABOUT_CENTER) {
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         AffineTransform original = g2.getTransform();
         AffineTransform at = new AffineTransform();
         at.concatenate(original);
         at.translate((double)((this.getIconWidth() - this.icon.getIconWidth()) / 2), (double)((this.getIconHeight() - this.icon.getIconHeight()) / 2));
         at.rotate(Math.toRadians(this.angle), (double)(x + cWidth), (double)(y + cHeight));
         g2.setTransform(at);
         this.icon.paintIcon(c, g2, x, y);
         g2.setTransform(original);
      }

   }

   public static enum Rotate {
      DOWN,
      UP,
      UPSIDE_DOWN,
      ABOUT_CENTER;
   }
}
