package ru.turikhay.tlauncher.ui.background;

import java.awt.Color;
import java.awt.Graphics;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public abstract class Background extends ExtendedLayeredPane {
   private static final long serialVersionUID = -1353975966057230209L;
   protected Color coverColor;

   public Background(BackgroundHolder holder, Color coverColor) {
      super(holder);
      this.coverColor = coverColor;
   }

   public Color getCoverColor() {
      return this.coverColor;
   }

   public final void paint(Graphics g) {
      this.paintBackground(g);
      super.paint(g);
   }

   public abstract void paintBackground(Graphics var1);
}
