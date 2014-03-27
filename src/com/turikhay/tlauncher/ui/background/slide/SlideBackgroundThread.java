package com.turikhay.tlauncher.ui.background.slide;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

public class SlideBackgroundThread {
   private static final Pattern filePattern = Pattern.compile("^.+\\.(?:jp(?:e|)g|png)$");
   private final SlideBackground background;
   final Slide defaultSlide;
   private Slide currentSlide;

   SlideBackgroundThread(SlideBackground background) {
      this.background = background;
      this.defaultSlide = new Slide(ImageCache.getRes("skyland.jpg"));
   }

   public SlideBackground getBackground() {
      return this.background;
   }

   public Slide getSlide() {
      return this.currentSlide;
   }

   public synchronized void refreshSlide(boolean animate) {
      String path = TLauncher.getInstance().getSettings().get("gui.background");
      URL url = this.getImageURL(path);
      Slide slide = url == null ? this.defaultSlide : new Slide(url);
      if (slide.isLocal()) {
         this.setSlide(slide, animate);
      } else {
         this.asyncSetSlide(slide, true);
      }

   }

   public synchronized void setSlide(Slide slide, boolean animate) {
      if (slide == null) {
         throw new NullPointerException();
      } else if (!slide.equals(this.currentSlide)) {
         Image image = slide.getImage();
         if (image == null) {
            slide = this.defaultSlide;
            image = slide.getImage();
         }

         this.currentSlide = slide;
         this.background.holder.cover.makeCover(animate);
         this.background.setImage(image);
         this.background.holder.cover.removeCover(animate);
      }
   }

   public void asyncSetSlide(final Slide slide, final boolean animate) {
      AsyncThread.execute(new Runnable() {
         public void run() {
            SlideBackgroundThread.this.setSlide(slide, animate);
         }
      });
   }

   private URL getImageURL(String path) {
      this.log("Trying to resolve path:", path);
      if (path == null) {
         this.log("Na NULL i suda NULL.");
         return null;
      } else {
         URL asURL = U.makeURL(path);
         if (asURL != null) {
            this.log("Path resolved as URL:", asURL);
            return asURL;
         } else {
            File asFile = new File(path);
            if (asFile.isFile()) {
               String absPath = asFile.getAbsolutePath();
               this.log("Path resolved as a file:", absPath);
               if (!filePattern.matcher(absPath).matches()) {
                  this.log("This file doesn't seem to be an image. It should have JPG or PNG format.");
                  return null;
               } else {
                  try {
                     return asFile.toURI().toURL();
                  } catch (IOException var6) {
                     this.log("Cannot covert this file into URL.", var6);
                     return null;
                  }
               }
            } else {
               this.log("Cannot resolve this path.");
               return null;
            }
         }
      }
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
