package ru.turikhay.tlauncher.ui.swing;

import java.awt.Insets;
import ru.turikhay.tlauncher.ui.TLauncherFrame;

public class MagnifiedInsets extends Insets {
   public MagnifiedInsets(int top, int left, int bottom, int right) {
      super((int)((double)top * TLauncherFrame.magnifyDimensions), (int)((double)left * TLauncherFrame.magnifyDimensions), (int)((double)bottom * TLauncherFrame.magnifyDimensions), (int)((double)right * TLauncherFrame.magnifyDimensions));
   }

   public static MagnifiedInsets get(Insets insets) {
      if (insets == null) {
         throw new NullPointerException();
      } else {
         return insets instanceof MagnifiedInsets ? (MagnifiedInsets)insets : new MagnifiedInsets(insets.top, insets.left, insets.bottom, insets.right);
      }
   }
}
