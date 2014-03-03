package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.LayoutManager;
import javax.swing.BoxLayout;

public class VPanel extends ExtendedPanel {
   private static final long serialVersionUID = -7956156442842177101L;

   public VPanel(boolean isDoubleBuffered) {
      super(isDoubleBuffered);
      this.setLayout(new BoxLayout(this, 3));
   }

   public VPanel() {
      this(true);
   }

   public BoxLayout getLayout() {
      return (BoxLayout)super.getLayout();
   }

   public void setLayout(LayoutManager mgr) {
      if (mgr instanceof BoxLayout) {
         super.setLayout(mgr);
      }

   }
}
