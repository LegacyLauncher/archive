package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.BoxLayout;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.block.BlockablePanel;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.UnblockablePanel;
import ru.turikhay.util.U;

public class CenterPanel extends BlockablePanel {
   private static final long serialVersionUID = -1975869198322761508L;
   public static final CenterPanelTheme defaultTheme = new DefaultCenterPanelTheme();
   public static final CenterPanelTheme tipTheme = new TipPanelTheme();
   public static final CenterPanelTheme loadingTheme = new LoadingPanelTheme();
   public static final CenterPanelTheme settingsTheme = new SettingsPanelTheme();
   public static final Insets defaultInsets = new MagnifiedInsets(5, 24, 18, 24);
   public static final Insets squareInsets = new MagnifiedInsets(15, 15, 15, 15);
   public static final Insets smallSquareInsets = new MagnifiedInsets(7, 7, 7, 7);
   public static final Insets smallSquareNoTopInsets = new MagnifiedInsets(5, 15, 5, 15);
   public static final Insets noInsets = new MagnifiedInsets(0, 0, 0, 0);
   protected static final int ARC_SIZE = 24;
   private final Insets insets;
   private final CenterPanelTheme theme;
   protected final ExtendedPanel messagePanel;
   protected final LocalizableLabel messageLabel;
   public final TLauncher tlauncher;
   public final Configuration global;
   public final LangConfiguration lang;

   public CenterPanel() {
      this((CenterPanelTheme)null, (Insets)null);
   }

   public CenterPanel(Insets insets) {
      this((CenterPanelTheme)null, insets);
   }

   public CenterPanel(CenterPanelTheme theme) {
      this(theme, (Insets)null);
   }

   public CenterPanel(CenterPanelTheme theme, Insets insets) {
      this.tlauncher = TLauncher.getInstance();
      this.global = this.tlauncher.getSettings();
      this.lang = this.tlauncher.getLang();
      CenterPanelTheme var10001 = theme == null ? defaultTheme : theme;
      theme = var10001;
      this.theme = var10001;
      Insets var10001 = insets == null ? defaultInsets : MagnifiedInsets.get(insets);
      if (insets == null) {
         Insets var10000 = defaultInsets;
      } else {
         MagnifiedInsets.get(insets);
      }

      this.insets = (Insets)var10001;
      this.setLayout(new BoxLayout(this, 3));
      this.setBackground(theme.getPanelBackground());
      this.messageLabel = new LocalizableLabel("  ");
      this.messageLabel.setFont(this.getFont().deriveFont(1));
      this.messageLabel.setVerticalAlignment(0);
      this.messageLabel.setHorizontalTextPosition(0);
      this.messageLabel.setAlignmentX(0.5F);
      this.messagePanel = new ExtendedPanel();
      this.messagePanel.setLayout(new BoxLayout(this.messagePanel, 1));
      this.messagePanel.setAlignmentX(0.5F);
      this.messagePanel.setInsets(new Insets(3, 0, 3, 0));
      this.messagePanel.add((Component)this.messageLabel);
   }

   public void paintComponent(Graphics g0) {
      Graphics2D g = (Graphics2D)g0;
      byte x = 0;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(this.getBackground());
      g.fillRoundRect(x, x, this.getWidth(), this.getHeight(), 24, 24);
      g.setColor(this.theme.getBorder());

      int var5;
      for(var5 = 1; var5 < 3; ++var5) {
         g.drawRoundRect(var5 - 1, var5 - 1, this.getWidth() - 2 * var5 + 1, this.getHeight() - 2 * var5 + 1, 24 - 2 * var5 + 1, 24 - 2 * var5 + 1);
      }

      Color shadow = U.shiftAlpha(Color.gray, -155);
      var5 = 3;

      while(true) {
         shadow = U.shiftAlpha(shadow, -10);
         if (shadow.getAlpha() == 0) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            super.paintComponent(g);
            return;
         }

         g.setColor(shadow);
         g.drawRoundRect(var5 - 1, var5 - 1, this.getWidth() - 2 * var5 + 1, this.getHeight() - 2 * var5 + 1, 24 - 2 * var5 + 1, 24 - 2 * var5 + 1);
         ++var5;
      }
   }

   public CenterPanelTheme getTheme() {
      return this.theme;
   }

   public Insets getInsets() {
      return this.insets;
   }

   protected Del del(int aligment) {
      return new Del(1, aligment, this.theme.getBorder());
   }

   protected Del del(int aligment, int width, int height) {
      return new Del(1, aligment, width, height, this.theme.getBorder());
   }

   public void defocus() {
      this.requestFocusInWindow();
   }

   public boolean setError(String message) {
      this.messageLabel.setForeground(this.theme.getFailure());
      this.messageLabel.setText(message != null && message.length() != 0 ? message : " ");
      return false;
   }

   protected boolean setMessage(String message, Object... vars) {
      this.messageLabel.setForeground(this.theme.getFocus());
      this.messageLabel.setText(message != null && message.length() != 0 ? message : " ", vars);
      return true;
   }

   protected boolean setMessage(String message) {
      return this.setMessage(message, Localizable.EMPTY_VARS);
   }

   public static BlockablePanel sepPan(LayoutManager manager, Component... components) {
      BlockablePanel panel = new BlockablePanel(manager) {
         private static final long serialVersionUID = 1L;

         public Insets getInsets() {
            return CenterPanel.noInsets;
         }
      };
      panel.add(components);
      return panel;
   }

   public static BlockablePanel sepPan(Component... components) {
      return sepPan(new GridLayout(0, 1), components);
   }

   public static UnblockablePanel uSepPan(LayoutManager manager, Component... components) {
      UnblockablePanel panel = new UnblockablePanel(manager) {
         private static final long serialVersionUID = 1L;

         public Insets getInsets() {
            return CenterPanel.noInsets;
         }
      };
      panel.add(components);
      return panel;
   }

   public static UnblockablePanel uSepPan(Component... components) {
      return uSepPan(new GridLayout(0, 1), components);
   }

   protected void log(Object... o) {
      U.log("[" + this.getClass().getSimpleName() + "]", o);
   }
}
