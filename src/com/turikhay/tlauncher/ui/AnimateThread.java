package com.turikhay.tlauncher.ui;

import javax.swing.JComponent;

public class AnimateThread {
   public static final long DEFAULT_TIME = 1000L;

   public static void animate(JComponent comp, int destX, int destY, long ms) {
      comp.setLocation(destX, destY);
   }

   public static void animate(JComponent comp, int destX, int destY) {
      animate(comp, destX, destY, 1000L);
   }
}
