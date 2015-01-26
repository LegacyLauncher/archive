package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.image.ImageObserver;
import ru.turikhay.tlauncher.ui.background.Background;
import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;

public class SlideBackground extends Background {
   private static final long serialVersionUID = -4479685866688951989L;
   private final SlideBackgroundThread thread;
   final BackgroundHolder holder;
   final ExtendedComponentAdapter listener;
   private Image oImage;
   private int oImageWidth;
   private int oImageHeight;
   private Image vImage;
   private int vImageWidth;
   private int vImageHeight;

   public SlideBackground(BackgroundHolder holder) {
      super(holder, Color.black);
      this.holder = holder;
      this.thread = new SlideBackgroundThread(this);
      this.thread.setSlide(this.thread.defaultSlide, false);
      this.thread.refreshSlide(false);
      this.listener = new ExtendedComponentAdapter(this, 1000) {
         public void onComponentResized(ComponentEvent e) {
            SlideBackground.this.updateImage();
            SlideBackground.this.repaint();
         }
      };
      this.addComponentListener(this.listener);
   }

   public SlideBackgroundThread getThread() {
      return this.thread;
   }

   public Image getImage() {
      return this.oImage;
   }

   public void setImage(Image image) {
      if (image == null) {
         throw new NullPointerException();
      } else {
         this.oImage = image;
         this.oImageWidth = image.getWidth((ImageObserver)null);
         this.oImageHeight = image.getHeight((ImageObserver)null);
         this.updateImage();
      }
   }

   private void updateImage() {
      double windowWidth = (double)this.getWidth();
      double windowHeight = (double)this.getHeight();
      double ratio = Math.min((double)this.oImageWidth / windowWidth, (double)this.oImageHeight / windowHeight);
      double width;
      double height;
      if (ratio < 1.0D) {
         width = (double)this.oImageWidth;
         height = (double)this.oImageHeight;
      } else {
         width = (double)this.oImageWidth / ratio;
         height = (double)this.oImageHeight / ratio;
      }

      this.vImageWidth = (int)width;
      this.vImageHeight = (int)height;
      if (this.vImageWidth != 0 && this.vImageHeight != 0) {
         if (this.oImageWidth == this.vImageWidth && this.oImageHeight == this.vImageHeight) {
            this.vImage = this.oImage;
         } else {
            this.vImage = this.oImage.getScaledInstance(this.vImageWidth, this.vImageHeight, 4);
         }
      } else {
         this.vImage = null;
      }

   }

   public void paintBackground(Graphics g) {
      if (this.vImage == null) {
         this.updateImage();
      }

      if (this.vImage != null) {
         double windowWidth = (double)this.getWidth();
         double windowHeight = (double)this.getHeight();
         double ratio = Math.min((double)this.vImageWidth / windowWidth, (double)this.vImageHeight / windowHeight);
         double width = (double)this.vImageWidth / ratio;
         double height = (double)this.vImageHeight / ratio;
         double x = (windowWidth - width) / 2.0D;
         double y = (windowHeight - height) / 2.0D;
         g.drawImage(this.vImage, (int)x, (int)y, (int)width, (int)height, (ImageObserver)null);
      }
   }
}
