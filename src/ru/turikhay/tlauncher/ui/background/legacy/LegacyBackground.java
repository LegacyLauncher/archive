package ru.turikhay.tlauncher.ui.background.legacy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.U;

public class LegacyBackground extends PaintBackground {
   private static final long serialVersionUID = -3732711088655124975L;
   private Image grass = Images.getImage("grass.png");
   private Image sun = Images.getImage("sun.png");
   private Image glow = Images.getImage("glow.png");
   private final int grassW;
   private final int grassH;
   private final int sunW;
   private final int sunH;
   private final int glowW;
   private final int glowH;
   double sunLocation;
   private Color backgroundColor = new Color(141, 189, 233);

   public LegacyBackground(BackgroundHolder holder, double loc) {
      super(holder);
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

   protected void draw(Graphics2D g, boolean force) {
      this.drawGrass(g);
      this.drawGlow(g);
      this.drawSun(g);
   }

   void drawGrass(Graphics2D g) {
      for(int rw = 0; rw <= this.width; rw += this.grassW) {
         g.drawImage(this.grass, rw, this.height - this.grassH, (ImageObserver)null);
      }

   }

   void drawSun(Graphics2D g) {
      int x = (int)((double)this.width * this.sunLocation - (double)(this.sunW / 2));
      int y = this.height - this.grassH - this.sunH;
      g.drawImage(this.sun, x, y, this.sunW, this.sunH, (ImageObserver)null);
      g.setColor(this.backgroundColor);
      g.fillRect(0, 0, x, this.height - this.grassH - this.glowH);
      g.fillRect(0, 0, this.width, y);
      g.fillRect(x + this.sunW, y, this.sunW, this.sunH - this.glowH);
   }

   void drawGlow(Graphics2D g) {
      for(int rw = 0; rw <= this.width; rw += this.glowW) {
         g.drawImage(this.glow, rw, this.height - this.grassH - this.glowH, (ImageObserver)null);
      }

   }
}
