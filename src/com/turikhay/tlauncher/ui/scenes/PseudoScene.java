package com.turikhay.tlauncher.ui.scenes;

import com.turikhay.tlauncher.ui.AnimatedVisibility;
import com.turikhay.tlauncher.ui.ExtendedLayeredPane;
import com.turikhay.tlauncher.ui.MainPane;

public abstract class PseudoScene extends ExtendedLayeredPane implements AnimatedVisibility {
   private static final long serialVersionUID = -1L;
   protected final MainPane main;
   private boolean shown = true;

   public PseudoScene(MainPane main) {
      super(main);
      this.main = main;
      this.setBounds(0, 0, main.getWidth(), main.getHeight());
   }

   public MainPane getMainPane() {
      return this.main;
   }

   public void setShown(boolean shown) {
      this.setShown(shown, true);
   }

   public void setShown(boolean shown, boolean animate) {
      if (this.shown != shown) {
         this.shown = shown;
         this.setVisible(shown);
      }
   }
}
