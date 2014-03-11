package com.turikhay.tlauncher.ui.background.slide;

import com.turikhay.tlauncher.ui.background.Background;
import com.turikhay.tlauncher.ui.background.BackgroundHolder;
import com.turikhay.tlauncher.ui.images.ImageCache;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

public class SlideBackground extends Background {
   private static final long serialVersionUID = 5300511927922393183L;
   private final Image image = ImageCache.getImage("skyland.jpg");
   private final double imageWidth;
   private final double imageHeight;

   public SlideBackground(BackgroundHolder holder) {
      super(holder, Color.white);
      this.imageWidth = (double)this.image.getWidth((ImageObserver)null);
      this.imageHeight = (double)this.image.getHeight((ImageObserver)null);
   }

   public void paintBackground(Graphics g) {
      double windowWidth = (double)this.getWidth();
      double windowHeight = (double)this.getHeight();
      double ratio = Math.min(this.imageWidth / windowWidth, this.imageHeight / windowHeight);
      double width;
      double height;
      if (ratio < 1.0D) {
         width = this.imageWidth;
         height = this.imageHeight;
      } else {
         width = this.imageWidth / ratio;
         height = this.imageHeight / ratio;
      }

      double x = (windowWidth - width) / 2.0D;
      double y = (windowHeight - height) / 2.0D;
      g.drawImage(this.image, (int)x, (int)y, (int)width, (int)height, (ImageObserver)null);
   }
}
