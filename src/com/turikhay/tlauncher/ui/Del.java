package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;

public class Del extends Panel {
   public static final int TOP = -1;
   public static final int CENTER = 0;
   public static final int BOTTOM = 1;
   private static final long serialVersionUID = -2252007533187803762L;
   private int size;
   private int aligment;
   private Color color;

   public Del(int size, int aligment, Color color) {
      this.size = size;
      this.aligment = aligment;
      this.color = color;
   }

   public void paint(Graphics g) {
      g.setColor(this.color);
      switch(this.aligment) {
      case -1:
         g.fillRect(0, 0, this.getWidth(), this.size);
         break;
      case 0:
         g.fillRect(0, this.getHeight() / 2 - this.size, this.getWidth(), this.size);
         break;
      case 1:
         g.fillRect(0, this.getHeight() - this.size, this.getWidth(), this.size);
      }

   }
}
