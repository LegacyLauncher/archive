package com.turikhay.tlauncher.ui.background;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.U;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;

public abstract class LightBackground extends PaintBackground {
   private static final long serialVersionUID = -3732711088655124975L;
   protected Image grass = ImageCache.getImage("grass.png");
   protected Image sun = ImageCache.getImage("sun.png");
   protected Image glow = ImageCache.getImage("glow.png");
   protected final int grassW;
   protected final int grassH;
   protected final int sunW;
   protected final int sunH;
   protected final int glowW;
   protected final int glowH;
   protected double sunLocation;
   protected Color backgroundColor = new Color(141, 189, 233);

   public LightBackground(MainPane main, double loc) {
      super(main);
      this.setBackground(this.backgroundColor);
      this.grassW = this.grass.getWidth((ImageObserver)null);
      this.grassH = this.grass.getHeight((ImageObserver)null);
      this.sunW = this.sun.getWidth((ImageObserver)null);
      this.sunH = this.sun.getHeight((ImageObserver)null);
      this.glowW = this.glow.getWidth((ImageObserver)null);
      this.glowH = this.glow.getHeight((ImageObserver)null);
      this.relativeSize = 0.5D;
      this.sunLocation = loc <= 0.0D ? U.doubleRandom() : loc;
      if (this.sunLocation <= 0.0D) {
         this.sunLocation += 0.5D;
      }

   }

   public void draw(Graphics2D g, boolean force) {
      this.drawGrass(g);
      this.drawGlow(g);
      this.drawSun(g);
   }

   public void drawGrass(Graphics2D g) {
      for(int rw = 0; rw <= this.width; rw += this.grassW) {
         g.drawImage(this.grass, rw, this.height - this.grassH, (ImageObserver)null);
      }

   }

   public void drawSun(Graphics2D g) {
      int x = (int)((double)this.width * this.sunLocation - (double)(this.sunW / 2));
      int y = this.height - this.grassH - this.sunH;
      g.drawImage(this.sun, x, y, this.sunW, this.sunH, (ImageObserver)null);
      g.setColor(this.backgroundColor);
      g.fillRect(0, 0, x, this.height - this.grassH - this.glowH);
      g.fillRect(0, 0, this.width, y);
      g.fillRect(x + this.sunW, y, this.sunW, this.sunH - this.glowH);
   }

   public void drawGlow(Graphics2D g) {
      for(int rw = 0; rw <= this.width; rw += this.glowW) {
         g.drawImage(this.glow, rw, this.height - this.grassH - this.glowH, (ImageObserver)null);
      }

   }
}
