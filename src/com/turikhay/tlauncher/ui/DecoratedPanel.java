package com.turikhay.tlauncher.ui;

import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.ImageObserver;

public class DecoratedPanel extends Panel {
   private static final long serialVersionUID = 1L;
   protected Background bg;

   public Background getPanelBackground() {
      return this.bg;
   }

   public void setPanelBackground(Background bg) {
      this.bg = bg;
   }

   public void update(Graphics g0) {
      this.paint(g0);
   }

   public void paint(Graphics g0) {
      g0.drawImage(this.bg.draw(g0), 0, 0, this.getWidth(), this.getHeight(), (ImageObserver)null);
   }
}
