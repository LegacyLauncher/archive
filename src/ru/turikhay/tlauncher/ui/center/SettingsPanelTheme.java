package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class SettingsPanelTheme extends DefaultCenterPanelTheme {
   protected final Color panelBackgroundColor = new Color(255, 255, 255, 128);
   protected final Color borderColor = new Color(172, 172, 172, 255);
   protected final Color delPanelColor = new Color(50, 80, 190, 255);

   public Color getPanelBackground() {
      return this.panelBackgroundColor;
   }

   public Color getBorder() {
      return this.borderColor;
   }

   public Color getDelPanel() {
      return this.delPanelColor;
   }
}
