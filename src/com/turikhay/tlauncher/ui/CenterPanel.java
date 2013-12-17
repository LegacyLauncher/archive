package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public abstract class CenterPanel extends BlockablePanel implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   private static List panels = new ArrayList();
   protected Insets insets = new Insets(5, 24, 18, 24);
   final CenterPanel instance = this;
   final TLauncher t;
   final TLauncherFrame f;
   final GlobalSettings s;
   final Settings l;
   Color panelColor = Color.getHSBColor(0.0F, 0.0F, 0.5F);
   Color delPanelColor = Color.getHSBColor(0.0F, 0.0F, 0.25F);
   Color successColor = Color.getHSBColor(0.25F, 0.66F, 0.66F);
   Color errorColor = Color.getHSBColor(0.0F, 0.66F, 0.66F);
   Color borderColor;
   Color textBackground;
   Color textForeground;
   Color wrongColor;
   Font font;
   Font font_italic;
   Font font_bold;
   Font font_error;
   Font font_small;
   int fontsize;
   String fontname;
   JPanel error;
   LocalizableLabel error_l;

   public CenterPanel(TLauncherFrame f) {
      this.borderColor = this.successColor;
      this.textBackground = Color.white;
      this.textForeground = Color.black;
      this.wrongColor = Color.pink;
      this.error = new JPanel();
      LayoutManager layout = new BoxLayout(this, 3);
      this.setLayout(layout);
      this.setBackground(new Color(255, 255, 255, 128));
      this.setOpaque(false);
      this.f = f;
      this.t = this.f.t;
      this.s = this.t.getSettings();
      this.l = f.lang;
      this.font = this.getFont();
      if (this.font == null) {
         this.font = new Font("", 0, 15);
      }

      this.setFont(this.font);
      this.fontsize = this.font.getSize();
      this.fontname = this.font.getName();
      this.font_italic = new Font(this.fontname, 2, this.fontsize);
      this.font_bold = new Font(this.fontname, 1, this.fontsize);
      this.font_small = new Font(this.fontname, 0, this.fontsize > 5 ? this.fontsize - 2 : this.fontsize);
      this.error_l = new LocalizableLabel(" ");
      this.error_l.setFont(this.font_bold);
      this.error_l.setForeground(new Color(8388608));
      this.error_l.setVerticalAlignment(0);
      this.error.setOpaque(false);
      this.error.add(this.error_l);
      panels.add(this);
      this.add(Box.createVerticalGlue());
   }

   public void paintComponent(Graphics g0) {
      Graphics2D g = (Graphics2D)g0;
      int arc = 32;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(this.getBackground());
      g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arc, arc);
      g.setColor(this.borderColor);

      for(int x = 1; x < 2; ++x) {
         g.drawRoundRect(x - 1, x - 1, this.getWidth() - 2 * x + 1, this.getHeight() - 2 * x + 1, arc, arc);
      }

      super.paintComponent(g);
   }

   protected FlowLayout getDefaultFlowLayout(int aligment) {
      FlowLayout t = new FlowLayout();
      t.setVgap(0);
      t.setHgap(0);
      t.setAlignment(aligment);
      return t;
   }

   public Insets getInsets() {
      return this.insets;
   }

   public boolean setError(String message) {
      this.error_l.setHorizontalAlignment(0);
      this.error_l.setHorizontalTextPosition(0);
      boolean repaint = false;
      if (message == null) {
         if (this.borderColor != this.successColor) {
            repaint = true;
         }

         this.borderColor = this.successColor;
         this.error_l.setText(" ");
      } else {
         if (this.borderColor != this.errorColor) {
            repaint = true;
         }

         this.borderColor = this.errorColor;
         this.error_l.setText(message);
      }

      if (repaint) {
         this.repaint();
      }

      return false;
   }

   protected Del del(int aligment) {
      return new Del(1, aligment, this.borderColor);
   }

   protected Del cdel(int aligment, int width, int height) {
      Del del = this.del(aligment);
      del.setPreferredSize(new Dimension(width, height));
      return del;
   }

   protected void defocus() {
      this.requestFocusInWindow();
   }

   public void updateLocale() {
      this.error_l.setText("");
   }

   protected void blockElement(Object reason) {
      handleComponents(this, false);
   }

   protected void unblockElement(Object reason) {
      handleComponents(this, true);
   }

   protected static void handleComponents(Container container, boolean setEnabled) {
      Component[] components = container.getComponents();
      Component[] var6 = components;
      int var5 = components.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Component component = var6[var4];
         component.setEnabled(setEnabled);
         if (component instanceof Container) {
            handleComponents((Container)component, setEnabled);
         }
      }

   }

   protected static JPanel createSeparateJPanel(Component... components) {
      BlockablePanel panel = new BlockablePanel() {
         private static final long serialVersionUID = 1L;

         protected void blockElement(Object reason) {
            CenterPanel.handleComponents(this, false);
         }

         protected void unblockElement(Object reason) {
            CenterPanel.handleComponents(this, true);
         }
      };
      LayoutManager lm = new GridLayout(0, 1);
      panel.setLayout(lm);
      panel.setOpaque(false);
      Component[] var6 = components;
      int var5 = components.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Component comp = var6[var4];
         panel.add(comp);
      }

      return panel;
   }

   protected static JPanel sepPan(Component... components) {
      return createSeparateJPanel(components);
   }
}
