package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class Slide {
   private final URL url;
   private Image image;

   Slide(URL url) {
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

   public URL getURL() {
      return this.url;
   }

   public boolean isLocal() {
      return this.url.getProtocol().equals("file");
   }

   public Image getImage() {
      if (this.image == null) {
         this.load();
      }

      return this.image;
   }

   private void load() {
      this.log("Loading from:", this.url);
      BufferedImage tempImage = null;

      try {
         tempImage = ImageIO.read(this.url);
      } catch (Throwable var3) {
         this.log("Cannot load slide!", var3);
         return;
      }

      if (tempImage == null) {
         this.log("Image seems to be corrupted.");
      } else {
         this.log("Loaded successfully!");
         this.image = tempImage;
      }
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
