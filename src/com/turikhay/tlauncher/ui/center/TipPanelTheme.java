package com.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class TipPanelTheme extends DefaultCenterPanelTheme {
   private final Color borderColor;

   public TipPanelTheme() {
      this.borderColor = this.failureColor;
   }

   public Color getBorder() {
      return this.borderColor;
   }
}
