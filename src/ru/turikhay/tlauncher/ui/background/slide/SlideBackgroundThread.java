package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.explorer.ImageFileFilter;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class SlideBackgroundThread extends LoopedThread {
   private static SlideBackgroundThread instance;
   private static final Pattern extensionPattern;
   private static final String defaultImageName = "plains.jpg";
   private final SlideBackground background;
   final Slide defaultSlide;
   private Slide currentSlide;

   static {
      extensionPattern = ImageFileFilter.extensionPattern;
   }

   public static void alert() {
      U.log(instance.defaultSlide, instance.currentSlide);
   }

   SlideBackgroundThread(SlideBackground background) {
      super("SlideBackgroundThread");
      instance = this;
      this.background = background;
      this.defaultSlide = new Slide(Images.getRes("plains.jpg"));
      this.startAndWait();
   }

   public SlideBackground getBackground() {
      return this.background;
   }

   public Slide getSlide() {
      return this.currentSlide;
   }

   public synchronized void refreshSlide(boolean animate) {
      URL url = this.getImageURL(TLauncher.getInstance().getSettings().get("gui.background"));
      Slide slide = url == null ? this.defaultSlide : new Slide(url);
      this.setSlide(slide, animate);
   }

   public void asyncRefreshSlide() {
      this.iterate();
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
         if (image == null) {
            this.log("Default image is NULL. Check accessibility to the JAR file of TLauncher.");
         } else {
            this.background.holder.cover.makeCover(animate);
            this.background.setImage(image);
            U.sleepFor(500L);
            this.background.holder.cover.removeCover(animate);
         }
      }
   }

   protected void iterateOnce() {
      this.refreshSlide(true);
   }

   private URL getImageURL(String path) {
      if (path == null) {
         return null;
      } else {
         URL asURL = U.makeURL(path);
         if (asURL != null) {
            this.log("Path resolved as an URL:", asURL);
            return asURL;
         } else {
            File asFile = new File(path);
            if (asFile.isFile()) {
               String absPath = asFile.getAbsolutePath();
               this.log("Path resolved as a file:", absPath);
               String ext = FileUtil.getExtension(asFile);
               if (ext != null && extensionPattern.matcher(ext).matches()) {
                  try {
                     return asFile.toURI().toURL();
                  } catch (IOException var7) {
                     this.log("Cannot covert this file into URL.", var7);
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
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
