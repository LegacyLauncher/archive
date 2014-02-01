package com.turikhay.tlauncher.ui.loc;

import javax.swing.JLabel;

public class LocalizableLabel extends JLabel implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   private String path;
   private String r0;
   private String r1;
   private Object w0;
   private Object w1;

   public LocalizableLabel() {
      this.setText();
   }

   public LocalizableLabel(String path) {
      this.path = path;
      this.setText();
   }

   public LocalizableLabel(String path, int aligment) {
      super("", aligment);
      this.path = path;
      this.setText();
   }

   public LocalizableLabel(int aligment) {
      super("", aligment);
      this.setText();
   }

   public void setText() {
      this.setText(this.path);
   }

   public void setText(String path) {
      this.path = path;
      this.u();
      super.setText(Localizable.get() == null ? path : Localizable.get(path));
   }

   public void setText(String path, String replace, Object with) {
      this.path = path;
      this.u();
      this.r0 = replace;
      this.w0 = with;
      super.setText(Localizable.get() == null ? path : Localizable.get().get(path, replace, with));
   }

   public void setText(String path, String replace0, Object with0, String replace1, Object with1) {
      this.path = path;
      this.u();
      this.r0 = replace0;
      this.w0 = with0;
      this.r1 = replace1;
      this.w1 = with1;
      super.setText(Localizable.get() == null ? path : Localizable.get().get(path, replace0, with0, replace1, with1));
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
