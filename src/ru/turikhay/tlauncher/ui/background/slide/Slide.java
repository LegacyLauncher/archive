package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.net.URL;
import javax.imageio.ImageIO;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class Slide {
   private final URL url;
   private SoftReference image = new SoftReference((Object)null);

   public Slide(URL url) {
      if (url == null) {
         throw new NullPointerException();
      } else {
         this.url = url;
         if (this.isLocal()) {
            this.load();
         }

      }
   }

   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else {
         Slide slide = (Slide)Reflect.cast(o, Slide.class);
         return slide == null ? false : this.url.equals(slide.url);
      }
   }

   public boolean isLocal() {
      return this.url.getProtocol().equals("file");
   }

   public Image getImage() {
      if (this.image.get() == null) {
         this.load();
      }

      return (Image)this.image.get();
   }

   private void load() {
      if (this.image.get() == null) {
         this.log("Loading image...");
         BufferedImage tempImage = null;
         long startTime = System.currentTimeMillis();

         try {
            tempImage = ImageIO.read(this.url);
         } catch (OutOfMemoryError var5) {
            this.log("Not enough space to load image, as it's is too big!", var5);
            return;
         } catch (Throwable var6) {
            this.log("Cannot load slide!", var6);
            return;
         }

         if (tempImage == null) {
            this.log("Image seems to be corrupted:", this.url);
         } else {
            this.log("Loaded successfully in", System.currentTimeMillis() - startTime, "ms.");
            this.image = new SoftReference(tempImage);
         }
      }

   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
