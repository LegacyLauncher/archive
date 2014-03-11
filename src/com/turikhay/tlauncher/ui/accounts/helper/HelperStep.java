package com.turikhay.tlauncher.ui.accounts.helper;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import java.awt.Component;

class HelperStep extends CenterPanel {
   private static final long serialVersionUID = -2410428259993661598L;
   public final String name;
   public final LocalizableLabel label;
   public final Component component;
   public final Component parent;
   public final byte alignment;
   public final HelperState[] states;

   HelperStep(String name, Component component, Component parent, byte alignment, HelperState... states) {
      super(tipTheme, smallSquareInsets);
      if (name == null) {
         throw new NullPointerException("Name is NULL");
      } else if (name.isEmpty()) {
         throw new IllegalArgumentException("Name is empty");
      } else if (component == null) {
         throw new NullPointerException("Component is NULL");
      } else if (parent == null) {
         throw new NullPointerException("Parent is NULL");
      } else if (alignment > 3) {
         throw new IllegalArgumentException("Unknown alignment");
      } else if (states == null) {
         throw new NullPointerException("State array is NULL");
      } else {
         this.name = name;
         this.component = component;
         this.parent = parent;
         this.alignment = alignment;
         this.label = new LocalizableLabel();
         this.states = states;
         this.add(this.label);
      }
   }
}
