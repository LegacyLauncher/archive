package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;

public abstract class CenterPanel extends BlockablePanel {
   private static final long serialVersionUID = 1L;
   protected Insets insets = new Insets(5, 24, 18, 24);
   final CenterPanel instance = this;
   final TLauncher t;
   final TLauncherFrame f;
   final GlobalSettings s;
   final Settings l;
   Color gray = Color.getHSBColor(0.0F, 0.0F, 0.4F);
   Color darkgray = Color.getHSBColor(0.0F, 0.0F, 0.25F);
   Color green = Color.getHSBColor(0.25F, 0.66F, 0.66F);
   Color red = Color.getHSBColor(0.0F, 0.66F, 0.66F);
   Color border = Color.getHSBColor(0.25F, 0.66F, 0.66F);
   Color white;
   Color black;
   Color pink;
   Font font;
   Font font_italic;
   Font font_bold;
   Font font_error;
   Font font_small;
   int fontsize;
   String fontname;
   Label error;

   public CenterPanel(TLauncherFrame f) {
      this.white = Color.white;
      this.black = Color.black;
      this.pink = Color.pink;
      LayoutManager layout = new BoxLayout(this, 3);
      this.setLayout(layout);
      this.setBackground(this.gray);
      this.f = f;
      this.t = this.f.t;
      this.s = this.t.settings;
      this.l = f.lang;
      this.font = this.getFont();
      if (this.font == null) {
         this.font = new Font("", 0, 12);
      }

      this.fontsize = this.font.getSize();
      this.fontname = this.font.getName();
      this.font_italic = new Font(this.fontname, 2, this.fontsize);
      this.font_bold = new Font(this.fontname, 1, this.fontsize);
      this.font_small = new Font(this.fontname, 0, this.fontsize > 5 ? this.fontsize - 2 : this.fontsize);
      this.error = new Label("");
      this.error.setFont(this.font_bold);
      this.error.setAlignment(1);
      this.error.setForeground(new Color(8388608));
   }

   protected FlowLayout getDefaultFlowLayout(int aligment) {
      FlowLayout t = new FlowLayout();
      t.setVgap(0);
      t.setHgap(0);
      t.setAlignment(aligment);
      return t;
   }

   public void update(Graphics g) {
      super.update(g);
      this.paint(g);
   }

   public void paint(Graphics g) {
      super.paint(g);
      g.setColor(this.border);

      for(int x = 1; x < 4; ++x) {
         g.drawRect(x - 1, x - 1, this.getWidth() - 2 * x, this.getHeight() - 2 * x);
      }

   }

   public Insets getInsets() {
      return this.insets;
   }

   public boolean setError(String message) {
      boolean repaint = false;
      if (message == null) {
         if (this.border != this.green) {
            repaint = true;
         }

         this.border = this.green;
         this.error.setText("");
      } else {
         if (this.border != this.red) {
            repaint = true;
         }

         this.border = this.red;
         this.error.setText(message);
      }

      if (repaint) {
         this.repaint();
      }

      return false;
   }

   protected Del del(int aligment) {
      return new Del(2, aligment, this.darkgray);
   }

   protected Del cdel(int aligment, int width, int height) {
      Del del = this.del(aligment);
      del.setPreferredSize(new Dimension(width, height));
      return del;
   }

   protected void defocus() {
      this.requestFocusInWindow();
   }
}
