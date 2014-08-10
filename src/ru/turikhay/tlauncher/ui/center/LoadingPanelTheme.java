package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class LoadingPanelTheme extends DefaultCenterPanelTheme {
   protected final Color panelBackgroundColor = new Color(255, 255, 255, 168);

   public Color getPanelBackground() {
      return this.panelBackgroundColor;
   }
}
