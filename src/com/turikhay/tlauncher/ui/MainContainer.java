package com.turikhay.tlauncher.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;

public class MainContainer extends Panel {
   private static final long serialVersionUID = 8925486339442046362L;
   TLauncherFrame f;
   VolatileImage vImage;
   Sun sun;
   private boolean drawSun = true;
   private boolean textChanged = false;
   private int lagmeter = 0;
   private int ow;
   private int oh;
   final MainContainer instance = this;
   private String text = null;
   Font font;

   MainContainer(TLauncherFrame f) {
      this.f = f;
      this.font = f.getFont();
      this.sun = new Sun(this);
      this.setBackground(f.bgcolor);
      this.setText(f.lang.get("hiddenText"));
      GridBagLayout gbl = new GridBagLayout();
      this.setLayout(gbl);
      this.add(f.lf);
   }

   public void update(Graphics g) {
      this.paint(g);
   }

   public void paint(Graphics g0) {
      int iw = this.getWidth();
      int w = iw / 2;
      int ih = this.getHeight();
      int h = ih / 2;
      boolean force = this.drawSun || this.ow != iw || this.oh != ih;
      this.ow = iw;
      this.oh = ih;
      if (this.vImage == null || this.vImage.getWidth() != w || this.vImage.getHeight() != h) {
         this.vImage = this.createVolatileImage(w, h);
      }

      Graphics g = this.vImage.getGraphics();
      if (this.drawSun) {
         long s = System.currentTimeMillis();
         this.sun.onPaint(w, h, g);
         long e = System.currentTimeMillis();
         long diff = e - s;
         if (diff > 1L) {
            if (this.lagmeter > 5) {
               this.sun.cancel();
               this.f.lf.setError(this.f.lang.get("sun.stopped"));
               this.drawSun = false;
            } else {
               ++this.lagmeter;
            }
         }
      }

      if (force) {
         int rw = 0;

         for(int x = 0; x <= w / this.f.bgimage.getWidth((ImageObserver)null); ++x) {
            g.drawImage(this.f.bgimage, rw, h - this.f.bgimage.getHeight((ImageObserver)null), (ImageObserver)null);
            rw += this.f.bgimage.getWidth((ImageObserver)null);
         }
      }

      if (force || this.textChanged) {
         if (this.text != null) {
            g.setFont(this.font);
            g.drawString(this.text, 0, g.getFontMetrics().getAscent());
         }

         this.textChanged = false;
      }

      g0.drawImage(this.vImage, 0, 0, w * 2, h * 2, (ImageObserver)null);
   }

   public void setText(String text) {
      this.text = text;
      this.textChanged = true;
   }

   public String getText() {
      return this.text;
   }
}
