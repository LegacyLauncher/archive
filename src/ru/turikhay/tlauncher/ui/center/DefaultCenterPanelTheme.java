package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class DefaultCenterPanelTheme extends CenterPanelTheme {
   protected final Color backgroundColor = new Color(255, 255, 255, 255);
   protected final Color panelBackgroundColor = new Color(255, 255, 255, 168);
   protected final Color focusColor = new Color(0, 0, 0, 255);
   protected final Color focusLostColor = new Color(128, 128, 128, 255);
   protected final Color successColor = new Color(78, 196, 78, 255);
   protected final Color failureColor = Color.getHSBColor(0.0F, 0.3F, 1.0F);
   protected final Color borderColor = new Color(28, 128, 28, 255);
   protected final Color delPanelColor;

   public DefaultCenterPanelTheme() {
      this.delPanelColor = this.successColor;
   }

   public Color getBackground() {
      return this.backgroundColor;
   }

   public Color getPanelBackground() {
      return this.panelBackgroundColor;
   }

   public Color getFocus() {
      return this.focusColor;
   }

   public Color getFocusLost() {
      return this.focusLostColor;
   }

   public Color getSuccess() {
      return this.successColor;
   }

   public Color getFailure() {
      return this.failureColor;
   }

   public Color getBorder() {
      return this.borderColor;
   }

   public Color getDelPanel() {
      return this.delPanelColor;
   }
}
