package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import ru.turikhay.tlauncher.ui.background.Background;
import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.util.U;

public class SlideBackground extends Background {
   private static final long serialVersionUID = -4479685866688951989L;
   private final SlideBackgroundThread thread;
   final BackgroundHolder holder;
   private Image image;
   private double imageWidth;
   private double imageHeight;

   public SlideBackground(BackgroundHolder holder) {
      super(holder, Color.black);
      this.holder = holder;
      this.thread = new SlideBackgroundThread(this);
      this.thread.setSlide(this.thread.defaultSlide, false);
      this.thread.refreshSlide(false);
   }

   public SlideBackgroundThread getThread() {
      return this.thread;
   }

   public Image getImage() {
      return this.image;
   }

   public void setImage(Image image) {
      if (image == null) {
         throw new NullPointerException();
      } else {
         this.image = image;
         this.imageWidth = (double)image.getWidth((ImageObserver)null);
         this.imageHeight = (double)image.getHeight((ImageObserver)null);
      }
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

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
