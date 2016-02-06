package ru.turikhay.tlauncher.ui.swing.util;

public enum Orientation {
   TOP(1),
   LEFT(2),
   BOTTOM(3),
   RIGHT(4),
   CENTER(0);

   private final int swingAlias;

   private Orientation(int swingAlias) {
      this.swingAlias = swingAlias;
   }

   public int getSwingAlias() {
      return this.swingAlias;
   }
}
