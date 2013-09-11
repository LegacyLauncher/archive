package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

public abstract class ExtendedTextField extends TextField {
   private static final long serialVersionUID = 1L;
   protected CenterPanel parent;
   protected Settings l;
   protected String value;
   protected String placeholder;
   protected boolean edit;
   private String error;
   protected FocusListener focusListener;
   protected TextListener textListener;

   ExtendedTextField(CenterPanel parentPanel, String placeholder, String text, int columns) {
      super(columns);
      this.parent = parentPanel;
      this.l = this.parent != null ? this.parent.l : null;
      this.placeholder = placeholder;
      this.setText(text);
      this.initListeners();
   }

   ExtendedTextField(String placeholder, String text, int columns) {
      this((CenterPanel)null, placeholder, text, columns);
   }

   ExtendedTextField(String text, int columns) {
      this((String)null, text, columns);
   }

   ExtendedTextField(CenterPanel parentPanel, String placeholder, String text) {
      this(parentPanel, placeholder, text, text != null ? text.length() : (placeholder != null ? placeholder.length() : 0));
   }

   ExtendedTextField(CenterPanel parentPanel, String placeholder) {
      this(parentPanel, placeholder, (String)null);
   }

   ExtendedTextField(String placeholder) {
      this((CenterPanel)null, placeholder, (String)null);
   }

   ExtendedTextField(CenterPanel parentPanel) {
      this(parentPanel, (String)null, (String)null);
   }

   public void setPlaceholder() {
      super.setText(this.placeholder);
      this.ok();
      if (this.parent != null) {
         this.setBackground(this.parent.white);
         this.setForeground(this.parent.gray);
         this.setFont(this.parent.font_italic);
      }
   }

   public void setPlaceholder(String placeholder) {
      this.placeholder = placeholder;
      this.setPlaceholder();
   }

   public void setText(String text) {
      this.setText((Object)text);
   }

   public void setText(Object text) {
      if (text == null && this.placeholder != null) {
         this.setPlaceholder();
      } else {
         this.onChangePrepare();
         String r = text != null ? text.toString() : null;
         this.value = r;
         super.setText(r);
      }

   }

   /** @deprecated */
   @Deprecated
   public String getText() {
      return !this.edit ? this.placeholder : super.getText();
   }

   public String getValue() {
      return !this.edit ? null : this.value;
   }

   public boolean check() {
      String text = this.getValue();
      return this.check(text) ? this.ok() : this.wrong(this.error);
   }

   protected boolean wrong(String reason) {
      if (this.parent == null) {
         return false;
      } else {
         this.setBackground(this.parent.pink);
         if (reason != null && reason != null) {
            this.parent.setError(reason);
         }

         return false;
      }
   }

   protected boolean ok() {
      if (this.parent == null) {
         return true;
      } else {
         this.setBackground(this.parent.white);
         this.setForeground(this.parent.black);
         this.setFont(this.parent.font);
         this.parent.setError((String)null);
         return true;
      }
   }

   protected void onFocusGained(FocusEvent e) {
      if (!this.edit) {
         this.edit = true;
         this.setText("");
      }
   }

   protected void onFocusLost(FocusEvent e) {
      String text = this.getValue();
      if (text == null || text.equals("")) {
         this.edit = false;
         this.setPlaceholder();
      }
   }

   protected void onChange(TextEvent e) {
      if (this.edit) {
         this.value = super.getText();
         if (!this.check()) {
            this.value = null;
         }

      }
   }

   protected void onChangePrepare() {
      if (!this.edit) {
         this.edit = true;
         this.setFont(this.parent.font);
      }
   }

   protected boolean setError(String error) {
      this.error = error;
      return false;
   }

   protected String getError() {
      return this.error;
   }

   private void initListeners() {
      this.addFocusListener(this.focusListener = new FocusListener() {
         public void focusGained(FocusEvent e) {
            ExtendedTextField.this.onFocusGained(e);
         }

         public void focusLost(FocusEvent e) {
            ExtendedTextField.this.onFocusLost(e);
         }
      });
      this.addTextListener(this.textListener = new TextListener() {
         public void textValueChanged(TextEvent e) {
            ExtendedTextField.this.onChange(e);
         }
      });
   }

   protected abstract boolean check(String var1);
}
