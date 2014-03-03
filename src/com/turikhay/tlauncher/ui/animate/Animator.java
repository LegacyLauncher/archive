package com.turikhay.tlauncher.ui.animate;

import java.awt.Component;

public class Animator {
   public static final int DEFAULT_TICK = 20;

   public static void move(Component comp, int destX, int destY, int tick) {
      comp.setLocation(destX, destY);
   }

   public static void move(Component comp, int destX, int destY) {
      move(comp, destX, destY, 20);
   }
}
