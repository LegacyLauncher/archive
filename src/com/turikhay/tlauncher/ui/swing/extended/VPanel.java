package com.turikhay.tlauncher.ui.swing.extended;

import com.turikhay.util.U;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;

public class VPanel extends ExtendedPanel {
   private static final long serialVersionUID = -7956156442842177101L;

   private VPanel(boolean isDoubleBuffered) {
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
         int axis = ((BoxLayout)U.getAs(mgr, BoxLayout.class)).getAxis();
         if (axis != 3 && axis != 1) {
            throw new IllegalArgumentException("Illegal BoxLayout axis!");
         } else {
            super.setLayout(mgr);
         }
      }
   }
}
