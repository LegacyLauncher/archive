package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;
import ru.turikhay.util.OS;

public class SettingsPanelTheme extends DefaultCenterPanelTheme {
   public SettingsPanelTheme() {
      this.borderColor = OS.VERSION.startsWith("10.") ? new Color(217, 217, 217, 255) : new Color(172, 172, 172, 255);
      this.delPanelColor = new Color(50, 80, 190, 255);
   }

   public Color getPanelBackground() {
      return this.panelBackgroundColor;
   }

   public Color getBorder() {
      return this.borderColor;
   }
}
