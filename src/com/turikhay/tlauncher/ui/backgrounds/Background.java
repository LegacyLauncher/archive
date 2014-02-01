package com.turikhay.tlauncher.ui.backgrounds;

import com.turikhay.tlauncher.ui.AnimatedVisibility;
import com.turikhay.tlauncher.ui.ExtendedLayeredPane;
import com.turikhay.tlauncher.ui.MainPane;

public abstract class Background extends ExtendedLayeredPane implements AnimatedVisibility, AnimatedBackground {
   private static final long serialVersionUID = -1353975966057230209L;

   public Background(MainPane main) {
      super(main);
   }

   public void setShown(boolean shown) {
      this.setShown(shown, true);
   }

   public void setShown(boolean shown, boolean animate) {
      this.setVisible(shown);
      if (shown) {
         this.start();
      } else {
         this.stop();
      }

   }
}
