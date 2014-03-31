package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

public class BorderPanel extends ExtendedPanel {
   private static final long serialVersionUID = -7641580330557833990L;

   private BorderPanel(BorderLayout layout, boolean isDoubleBuffered) {
      super(isDoubleBuffered);
      if (layout == null) {
         layout = new BorderLayout();
      }

      this.setLayout(layout);
   }

   public BorderPanel() {
      this((BorderLayout)null, true);
   }

   public BorderLayout getLayout() {
      return (BorderLayout)super.getLayout();
   }

   public void setLayout(LayoutManager mgr) {
      if (mgr instanceof BorderLayout) {
         super.setLayout(mgr);
      }

   }

   public int getHgap() {
      return this.getLayout().getHgap();
   }

   public void setHgap(int hgap) {
      this.getLayout().setHgap(hgap);
   }

   public int getVgap() {
      return this.getLayout().getVgap();
   }

   public void setVgap(int vgap) {
      this.getLayout().setVgap(vgap);
   }
}
