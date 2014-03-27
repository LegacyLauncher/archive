package com.turikhay.tlauncher.ui.background.slide;

import com.turikhay.util.U;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

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
         Slide slide = (Slide)U.getAs(o, Slide.class);
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
      } catch (IOException var6) {
         this.log("Cannot load slide!", var6);
         return;
      }

      if (tempImage == null) {
         this.log("Image seems to be corrupted.");
      } else {
         for(int x = 0; x < tempImage.getWidth(); ++x) {
            for(int y = 0; y < tempImage.getHeight(); ++y) {
               int rgba = tempImage.getRGB(x, y);
               Color col = new Color(rgba, true);
               col = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue());
               tempImage.setRGB(x, y, col.getRGB());
            }
         }

         this.log("Loaded successfully!");
         this.image = tempImage;
      }
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
