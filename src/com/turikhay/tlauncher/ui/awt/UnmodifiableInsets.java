package com.turikhay.tlauncher.ui.awt;

import java.awt.Insets;

public class UnmodifiableInsets extends Insets {
   private static final long serialVersionUID = 6086557765739009719L;
   public final int top;
   public final int left;
   public final int bottom;
   public final int right;

   public UnmodifiableInsets(int top, int left, int bottom, int right) {
      super(top, left, bottom, right);
      this.top = top;
      this.left = left;
      this.bottom = bottom;
      this.right = right;
   }

   public void set(int top, int left, int bottom, int right) {
      throw new UnsupportedOperationException("This instance of Insets is unmodifiable");
   }
}
