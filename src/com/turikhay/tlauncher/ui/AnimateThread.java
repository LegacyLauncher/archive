package com.turikhay.tlauncher.ui;

import java.awt.Component;

public class AnimateThread {
   public static final long DEFAULT_TIME = 1000L;

   public static void animate(Component comp, int destX, int destY, long ms) {
      comp.setLocation(destX, destY);
   }

   public static void animate(Component comp, int destX, int destY) {
      animate(comp, destX, destY, 1000L);
   }
}
