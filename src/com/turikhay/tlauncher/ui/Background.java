package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.U;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public abstract class Background {
   private static Map loaded = new HashMap();
   protected final DecoratedPanel comp;
   protected int width;
   protected int height;
   protected double relativeSize = 1.0D;
   protected VolatileImage vImage;

   protected Background(DecoratedPanel comp) {
      this.comp = comp;
   }

   public VolatileImage draw(Graphics g0) {
      int iw = this.comp.getWidth();
      int w = (int)((double)iw * this.relativeSize);
      int ih = this.comp.getHeight();
      int h = (int)((double)ih * this.relativeSize);
      boolean force = w != this.width || h != this.height;
      this.width = w;
      this.height = h;
      if (this.vImage == null || this.vImage.getWidth() != w || this.vImage.getHeight() != h) {
         this.vImage = this.comp.createVolatileImage(w, h);
      }

      Graphics2D g = (Graphics2D)this.vImage.getGraphics();
      this.draw(g, force);
      return this.vImage;
   }

   protected static BufferedImage loadImage(String name) {
      if (loaded.containsKey(name)) {
         return (BufferedImage)loaded.get(name);
      } else {
         try {
            Image i = ImageIO.read(TLauncherFrame.class.getResource(name));
            int w = i.getWidth((ImageObserver)null);
            int h = i.getHeight((ImageObserver)null);
            BufferedImage bi = new BufferedImage(w, h, 1);
            bi.getGraphics().drawImage(i, 0, 0, (ImageObserver)null);
            loaded.put(name, bi);
            return bi;
         } catch (IOException var5) {
            U.log("Cannot load required image", var5);
            return null;
         }
      }
   }

   protected abstract void draw(Graphics2D var1, boolean var2);
}
