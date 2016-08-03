package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;
import ru.turikhay.util.U;

public class LoadingPanelTheme extends DefaultCenterPanelTheme {
   public LoadingPanelTheme() {
      this.panelBackgroundColor = U.shiftAlpha(this.panelBackgroundColor, 40, 64, 176);
   }

   public Color getPanelBackground() {
      return this.panelBackgroundColor;
   }
}
