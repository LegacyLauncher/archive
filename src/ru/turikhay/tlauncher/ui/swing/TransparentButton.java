package ru.turikhay.tlauncher.ui.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.ButtonModel;
import javax.swing.JButton;

public class TransparentButton extends JButton {
   private static final long serialVersionUID = -5329305793566047719L;

   protected TransparentButton() {
      this.setBorderPainted(false);
      this.setContentAreaFilled(false);
      this.setFocusPainted(false);
      this.setOpaque(false);
      this.setForeground(Color.white);
      this.setPreferredSize(new Dimension(27, 27));
      this.setCursor(Cursor.getPredefinedCursor(12));
   }

   public TransparentButton(String text) {
      this();
      this.setText(text);
   }

   protected void paintComponent(Graphics g) {
      ButtonModel buttonModel = this.getModel();
      Graphics2D gd = (Graphics2D)g.create();
      gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gd.setPaint(new GradientPaint(0.0F, 0.0F, Color.decode("#67c7f4"), 0.0F, (float)this.getHeight(), Color.decode("#379fc9")));
      if (buttonModel.isRollover()) {
         gd.setPaint(new GradientPaint(0.0F, 0.0F, Color.decode("#7bd2f6"), 0.0F, (float)this.getHeight(), Color.decode("#43b3d5")));
         if (buttonModel.isPressed()) {
            gd.setPaint(new GradientPaint(0.0F, 0.0F, Color.decode("#379fc9"), 0.0F, (float)this.getHeight(), Color.decode("#4fb2dd")));
         } else {
            this.setForeground(Color.white);
         }
      }

      gd.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 8, 8);
      gd.dispose();
      super.paintComponent(g);
   }
}
