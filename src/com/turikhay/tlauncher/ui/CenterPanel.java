package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.Settings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Panel;
import javax.swing.BoxLayout;

public abstract class CenterPanel extends Panel {
   private static final long serialVersionUID = 3304521794866297004L;
   protected Insets insets = new Insets(5, 24, 18, 24);
   final CenterPanel instance = this;
   final TLauncher t;
   final TLauncherFrame f;
   final Settings s;
   final Settings l;
   Color gray = Color.getHSBColor(0.0F, 0.0F, 0.4F);
   Color darkgray = Color.getHSBColor(0.0F, 0.0F, 0.25F);
   Color green = Color.getHSBColor(0.25F, 0.66F, 0.66F);
   Color red = Color.getHSBColor(0.0F, 0.66F, 0.66F);
   Color border = Color.getHSBColor(0.25F, 0.66F, 0.66F);
   Font font;
   Font font_italic;
   Font font_bold;
   Font font_error;
   Font font_small;
   int fontsize;
   String fontname;
   Label error_l;
   LayoutManager g_zero = new GridLayout(0, 1);
   LayoutManager g_single = new GridLayout(1, 1);
   LayoutManager g_double = new GridLayout(0, 2);
   FlowLayout g_line = new FlowLayout();
   FlowLayout g_line_center = new FlowLayout();
   FlowLayout g_line_right = new FlowLayout();
   BorderLayout g_save = new BorderLayout();

   public CenterPanel(TLauncherFrame f) {
      LayoutManager layout = new BoxLayout(this, 3);
      this.setLayout(layout);
      this.setBackground(this.gray);
      this.f = f;
      this.t = this.f.t;
      this.s = this.t.settings;
      this.l = f.lang;
      this.g_line.setVgap(0);
      this.g_line_center.setVgap(0);
      this.g_line_right.setVgap(0);
      this.g_line.setHgap(0);
      this.g_line_center.setHgap(0);
      this.g_line_right.setHgap(0);
      this.g_line.setAlignment(3);
      this.g_line_center.setAlignment(1);
      this.g_line_right.setAlignment(2);
      this.g_save.setVgap(2);
      this.g_save.setHgap(3);
      this.font = this.getFont();
      if (this.font == null) {
         this.font = new Font("", 0, 12);
      }

      this.fontsize = this.font.getSize();
      this.fontname = this.font.getName();
      this.font_italic = new Font(this.fontname, 2, this.fontsize);
      this.font_bold = new Font(this.fontname, 1, this.fontsize);
      this.font_small = new Font(this.fontname, 0, this.fontsize > 5 ? this.fontsize - 2 : this.fontsize);
      this.error_l = new Label("");
      this.error_l.setFont(this.font_bold);
      this.error_l.setAlignment(1);
      this.error_l.setForeground(new Color(8388608));
   }

   public void update(Graphics g) {
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

   public void setError(String message) {
      if (message == null) {
         this.border = this.green;
         this.repaint();
         this.error_l.setText("");
      } else {
         this.border = this.red;
         this.repaint();
         this.error_l.setText(message);
      }
   }

   protected Panel del(int aligment) {
      return new Del(2, aligment, this.darkgray);
   }

   protected void defocus() {
      this.requestFocusInWindow();
   }

   protected abstract void block();

   protected abstract void unblock();
}
