package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.image.ImageObserver;
import java.lang.ref.SoftReference;
import ru.turikhay.tlauncher.ui.background.Background;
import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;

public class SlideBackground extends Background {
   private static final SoftReference nullImageReference = new SoftReference((Object)null);
   private final SlideBackgroundThread thread;
   final BackgroundHolder holder;
   final ExtendedComponentAdapter listener;
   private SoftReference oImage;
   private SoftReference vImage;
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
      return (Image)this.oImage.get();
   }

   public void setImage(Image image) {
      this.oImage = image == null ? nullImageReference : new SoftReference(image);
      this.updateImage();
   }

   private void updateImage() {
      double windowWidth = (double)this.getWidth();
      double windowHeight = (double)this.getHeight();
      Image _oImage = (Image)this.oImage.get();
      if (_oImage == null) {
         this.oImage = this.vImage = nullImageReference;
      } else {
         int oImageWidth = _oImage.getWidth((ImageObserver)null);
         int oImageHeight = _oImage.getHeight((ImageObserver)null);
         double ratio = Math.min((double)_oImage.getWidth((ImageObserver)null) / windowWidth, (double)_oImage.getHeight((ImageObserver)null) / windowHeight);
         double width;
         double height;
         if (ratio < 1.0D) {
            width = (double)oImageWidth;
            height = (double)oImageHeight;
         } else {
            width = (double)oImageWidth / ratio;
            height = (double)oImageHeight / ratio;
         }

         this.vImageWidth = (int)width;
         this.vImageHeight = (int)height;
         if (this.vImageWidth != 0 && this.vImageHeight != 0) {
            if (oImageWidth == this.vImageWidth && oImageHeight == this.vImageHeight) {
               this.vImage = this.oImage;
            } else {
               this.vImage = new SoftReference(_oImage.getScaledInstance(this.vImageWidth, this.vImageHeight, 4));
            }
         } else {
            this.vImage = nullImageReference;
         }

      }
   }

   public void paintBackground(Graphics g) {
      if (this.vImage.get() == null) {
         this.updateImage();
      }

      Image _vImage = (Image)this.vImage.get();
      double windowWidth = (double)this.getWidth();
      double windowHeight = (double)this.getHeight();
      if (_vImage == null) {
         g.drawRect(0, 0, (int)windowWidth, (int)windowHeight);
      } else {
         double ratio = Math.min((double)this.vImageWidth / windowWidth, (double)this.vImageHeight / windowHeight);
         double width = (double)this.vImageWidth / ratio;
         double height = (double)this.vImageHeight / ratio;
         double x = (windowWidth - width) / 2.0D;
         double y = (windowHeight - height) / 2.0D;
         g.drawImage(_vImage, (int)x, (int)y, (int)width, (int)height, (ImageObserver)null);
      }
   }
}
