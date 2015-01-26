package ru.turikhay.tlauncher.ui.swing.extended;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

public class WindowsTabbedPaneExtendedUI extends WindowsTabbedPaneUI implements ExtendedUI {
   public static final int ARC_SIZE = 16;
   public static final int Y_PADDING = 5;
   private CenterPanelTheme theme;

   public WindowsTabbedPaneExtendedUI(CenterPanelTheme theme) {
      this.theme = theme;
   }

   public WindowsTabbedPaneExtendedUI() {
      this((CenterPanelTheme)null);
   }

   public CenterPanelTheme getTheme() {
      return this.theme;
   }

   public void setTheme(CenterPanelTheme theme) {
      this.theme = theme;
   }

   protected void installDefaults() {
      super.installDefaults();
      this.contentBorderInsets = new Insets(7, 7, 7, 7);
      this.tabRunOverlay = 1;
      LookAndFeel.installProperty(this.tabPane, "opaque", Boolean.FALSE);
   }

   protected void paintContentBorder(Graphics g0, int tabPlacement, int selectedIndex) {
      Insets insets = this.tabPane.getInsets();
      Insets tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
      int x = insets.left;
      int y = insets.top;
      int w = this.tabPane.getWidth() - insets.right - insets.left;
      int h = this.tabPane.getHeight() - insets.top - insets.bottom;
      int tabWidth;
      if (tabPlacement != 2 && tabPlacement != 4) {
         tabWidth = this.calculateTabAreaHeight(tabPlacement, this.runCount, this.maxTabHeight);
         if (tabPlacement == 1) {
            y += tabWidth - tabAreaInsets.bottom;
         }

         h -= tabWidth - tabAreaInsets.bottom;
      } else {
         tabWidth = this.calculateTabAreaWidth(tabPlacement, this.runCount, this.maxTabWidth);
         if (tabPlacement == 2) {
            x += tabWidth - tabAreaInsets.bottom;
         }

         w -= tabWidth - tabAreaInsets.bottom;
      }

      Graphics2D g = (Graphics2D)g0;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Color background;
      Color border;
      if (this.theme == null) {
         background = this.tabPane.getBackground();
         border = this.tabPane.getForeground();
      } else {
         background = this.theme.getPanelBackground();
         border = this.theme.getBorder();
      }

      g.setColor(background);
      g.fillRoundRect(x, y - 5, w, h + 5, 16, 16);
      g.setColor(border);

      for(int i = 1; i < 2; ++i) {
         g.drawRoundRect(x + i - 1, y + i - 5 - 1, w - 2 * i + 1, h - 2 * i + 1 + 5, 16, 16);
      }

   }
}
