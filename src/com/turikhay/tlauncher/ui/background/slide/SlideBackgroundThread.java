package com.turikhay.tlauncher.ui.background.slide;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.explorer.ImageFileFilter;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
import com.turikhay.util.async.ExtendedThread;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

public class SlideBackgroundThread extends ExtendedThread {
   private static final String REFRESH_BLOCK = "refresh";
   private static final Pattern extensionPattern;
   private final SlideBackground background;
   final Slide defaultSlide;
   private Slide currentSlide;

   static {
      extensionPattern = ImageFileFilter.extensionPattern;
   }

   SlideBackgroundThread(SlideBackground background) {
      super("SlideBackgroundThread");
      this.background = background;
      this.defaultSlide = new Slide(ImageCache.getRes("skyland.jpg"));
      this.startAndWait();
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
      this.setSlide(slide, animate);
   }

   public void asyncRefreshSlide() {
      this.unblockThread("refresh");
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

   public void run() {
      while(true) {
         this.blockThread("refresh");
         this.refreshSlide(true);
      }
   }

   private URL getImageURL(String path) {
      this.log("Trying to resolve path:", path);
      if (path == null) {
         this.log("Na NULL i suda NULL.");
         return null;
      } else {
         File asFile = new File(path);
         if (asFile.isFile()) {
            String absPath = asFile.getAbsolutePath();
            this.log("Path resolved as a file:", absPath);
            String ext = FileUtil.getExtension(asFile);
            if (ext != null && extensionPattern.matcher(ext).matches()) {
               try {
                  return asFile.toURI().toURL();
               } catch (IOException var6) {
                  this.log("Cannot covert this file into URL.", var6);
                  return null;
               }
            } else {
               this.log("This file doesn't seem to be an image. It should have JPG or PNG format.");
               return null;
            }
         } else {
            this.log("Cannot resolve this path.");
            return null;
         }
      }
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
