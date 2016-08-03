package ru.turikhay.tlauncher.ui.background;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.util.U;

public final class ImageBackground extends JComponent implements ISwingBackground {
   private SoftReference defaultImage;
   private SoftReference currentImage;
   private SoftReference renderImage;
   private boolean paused;

   ImageBackground() {
      this.addComponentListener(new ExtendedComponentAdapter(this) {
         public void onComponentResized(ComponentEvent e) {
            ImageBackground.this.updateRender();
         }
      });
   }

   public void onResize() {
      if (this.getParent() != null) {
         this.setSize(this.getParent().getSize());
      }

   }

   public void startBackground() {
      this.paused = false;
      this.updateRender();
   }

   public void pauseBackground() {
      this.paused = true;
   }

   public void loadBackground(String path) throws Exception {
      if (this.defaultImage == null) {
         this.defaultImage = new SoftReference(Images.getImage("plains.jpg"));
      }

      this.renderImage = null;
      if (path == null) {
         this.currentImage = this.defaultImage;
      } else {
         this.currentImage = null;
         InputStream input = null;
         if (U.makeURL(path) == null) {
            File file = new File(path);
            if (file.isFile()) {
               input = new FileInputStream(file);
            }
         } else {
            input = U.makeURL(path).openStream();
         }

         if (input == null) {
            throw new IllegalArgumentException("could not parse path: " + path);
         }

         this.currentImage = new SoftReference(ImageIO.read((InputStream)input));
      }

      this.updateRender();
   }

   private void updateRender() {
      this.renderImage = null;
      Image image;
      if (this.currentImage != null && (image = (Image)this.currentImage.get()) != null) {
         this.renderImage = new SoftReference(image.getScaledInstance(this.getWidth(), this.getHeight(), 4));
         this.repaint();
      }
   }

   public void paint(Graphics g) {
      Image original;
      Image render;
      if (this.currentImage != null && (original = (Image)this.currentImage.get()) != null && this.renderImage != null && (render = (Image)this.renderImage.get()) != null) {
         double ratio = Math.min((double)original.getWidth((ImageObserver)null) / (double)this.getWidth(), (double)original.getHeight((ImageObserver)null) / (double)this.getHeight());
         double width = (double)original.getWidth((ImageObserver)null) / ratio;
         double height = (double)original.getHeight((ImageObserver)null) / ratio;
         double x = ((double)this.getWidth() - width) / 2.0D;
         double y = ((double)this.getHeight() - height) / 2.0D;
         g.drawImage(render, (int)x, (int)y, (int)width, (int)height, (ImageObserver)null);
      }
   }
}
