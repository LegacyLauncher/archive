package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.ui.TransparentButton;

public class LocalizableTransparentButton extends TransparentButton {
   private static final long serialVersionUID = 1L;
   static Settings l;
   private String path;
   private String r0;
   private String r1;
   private Object w0;
   private Object w1;

   public LocalizableTransparentButton(String path) {
      this.setText(path);
   }

   public LocalizableTransparentButton(String path, String replace, Object with) {
      this.setText(path, replace, with);
   }

   public void setText(String path) {
      this.path = path;
      super.setText(l == null ? path : l.get(path));
   }

   public void setText(String path, String replace, Object with) {
      this.path = path;
      this.u();
      this.r0 = replace;
      this.w0 = with;
      super.setText(l == null ? path : l.get(path, replace, with));
   }

   public void setText(String path, String replace0, Object with0, String replace1, Object with1) {
      this.path = path;
      this.u();
      this.r0 = replace0;
      this.w0 = with0;
      this.r1 = replace1;
      this.w1 = with1;
      super.setText(l == null ? path : l.get(path, replace0, with0, replace1, with1));
   }

   public String getLangPath() {
      return this.path;
   }

   public void updateLocale() {
      this.setText(this.path, this.r0, this.w0, this.r1, this.w1);
   }

   private void u() {
      this.r0 = this.r1 = null;
      this.w0 = this.w1 = null;
   }
}
