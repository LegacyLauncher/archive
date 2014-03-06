package com.turikhay.tlauncher.ui.background;

import com.turikhay.tlauncher.ui.MainPane;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;

public abstract class PaintBackground extends Background {
   private static final long serialVersionUID = 1251234865840478018L;
   protected int width;
   protected int height;
   protected double relativeSize = 1.0D;
   protected VolatileImage vImage;

   public PaintBackground(MainPane main) {
      super(main);
   }

   public void update(Graphics g0) {
      super.update(g0);
   }

   public void paint(Graphics g0) {
      g0.drawImage(this.draw(g0), 0, 0, this.getWidth(), this.getHeight(), (ImageObserver)null);
   }

   public VolatileImage draw(Graphics g0) {
      int iw = this.getWidth();
      int w = (int)((double)iw * this.relativeSize);
      int ih = this.getHeight();
      int h = (int)((double)ih * this.relativeSize);
      boolean force = w != this.width || h != this.height;
      this.width = w;
      this.height = h;
      if (this.vImage == null || this.vImage.getWidth() != w || this.vImage.getHeight() != h) {
         this.vImage = this.createVolatileImage(w, h);
      }

      Graphics2D g = (Graphics2D)this.vImage.getGraphics();
      this.draw(g, force);
      this.vImage.validate(this.getGraphicsConfiguration());
      return this.vImage;
   }

   protected abstract void draw(Graphics2D var1, boolean var2);
}
