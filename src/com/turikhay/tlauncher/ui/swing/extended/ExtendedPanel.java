package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import javax.swing.JPanel;

public class ExtendedPanel extends JPanel {
   private static final long serialVersionUID = 873670863629293560L;

   public ExtendedPanel(LayoutManager layout, boolean isDoubleBuffered) {
      super(layout, isDoubleBuffered);
      this.setOpaque(false);
   }

   public ExtendedPanel(LayoutManager layout) {
      this(layout, true);
   }

   public ExtendedPanel(boolean isDoubleBuffered) {
      this(new FlowLayout(), isDoubleBuffered);
   }

   public ExtendedPanel() {
      this(true);
   }

   public void add(Component... components) {
      if (components == null) {
         throw new NullPointerException();
      } else {
         Component[] var5 = components;
         int var4 = components.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component comp = var5[var3];
            this.add(comp);
         }

      }
   }

   public void add(Component component0, Component component1) {
      this.add(component0, component1);
   }
}
