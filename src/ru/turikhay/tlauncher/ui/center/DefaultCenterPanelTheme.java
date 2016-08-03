package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;
import javax.swing.JLabel;
import ru.turikhay.util.U;

public class DefaultCenterPanelTheme extends CenterPanelTheme {
   private final JLabel label = new JLabel();
   protected Color backgroundColor;
   protected Color panelBackgroundColor;
   protected Color focusColor;
   protected Color focusLostColor;
   protected Color successColor;
   protected Color failureColor;
   protected Color borderColor;
   protected Color delPanelColor;

   public DefaultCenterPanelTheme() {
      this.backgroundColor = this.label.getBackground();
      this.panelBackgroundColor = U.shiftAlpha(this.backgroundColor, -128, 64, 192);
      this.focusColor = this.label.getForeground();
      this.focusLostColor = U.shiftColor(this.focusColor, 96, 64, 192);
      this.successColor = new Color(78, 196, 78, 255);
      this.failureColor = Color.getHSBColor(0.0F, 1.0F, 0.7F);
      this.borderColor = new Color(28, 128, 28, 255);
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

   public Color getFailure() {
      return this.failureColor;
   }

   public Color getBorder() {
      return this.borderColor;
   }
}
