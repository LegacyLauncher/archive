package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import ru.turikhay.util.Reflect;

public class VPanel extends ExtendedPanel {
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
         int axis = ((BoxLayout)Reflect.cast(mgr, BoxLayout.class)).getAxis();
         if (axis != 3 && axis != 1) {
            throw new IllegalArgumentException("Illegal BoxLayout axis!");
         } else {
            super.setLayout(mgr);
         }
      }
   }
}
