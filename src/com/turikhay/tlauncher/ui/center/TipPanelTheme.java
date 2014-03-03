package com.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class TipPanelTheme extends DefaultCenterPanelTheme {
   public final Color panelBackgroundColor = new Color(255, 255, 255, 168);
   public final Color borderColor;

   public TipPanelTheme() {
      this.borderColor = this.failureColor;
   }

   public Color getPanelBackground() {
      return this.panelBackgroundColor;
   }

   public Color getBorder() {
      return this.borderColor;
   }
}
