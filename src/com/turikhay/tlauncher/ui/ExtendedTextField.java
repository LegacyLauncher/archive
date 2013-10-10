package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

public abstract class ExtendedTextField extends TextField implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   protected CenterPanel parent;
   protected Settings l;
   protected String value;
   protected String placeholder;
   protected boolean edit;
   private String error;
   protected FocusListener focusListener;
   protected TextListener textListener;

   protected ExtendedTextField() {
      this.initListeners();
   }

   public ExtendedTextField(CenterPanel parentPanel, String placeholder, String text, int columns) {
      super(columns);
      this.parent = parentPanel;
      this.l = this.parent != null ? this.parent.l : null;
      this.placeholder = placeholder;
      this.setText(text);
      this.initListeners();
   }

   public ExtendedTextField(String placeholder, String text, int columns) {
      this((CenterPanel)null, placeholder, text, columns);
   }

   public ExtendedTextField(String text, int columns) {
      this((String)null, text, columns);
   }

   public ExtendedTextField(CenterPanel parentPanel, String placeholder, String text) {
      this(parentPanel, placeholder, text, text != null ? text.length() : (placeholder != null ? placeholder.length() : 0));
   }

   public ExtendedTextField(CenterPanel parentPanel, String placeholder) {
      this(parentPanel, placeholder, (String)null);
   }

   public ExtendedTextField(String placeholder) {
      this((CenterPanel)null, placeholder, (String)null);
   }

   public ExtendedTextField(CenterPanel parentPanel) {
      this(parentPanel, (String)null, (String)null);
   }

   public void setPlaceholder() {
      super.setText(this.placeholder);
      this.ok();
      if (this.parent != null) {
         this.setBackground(this.parent.textBackground);
         this.setForeground(this.parent.panelColor);
         this.setFont(this.parent.font_italic);
      }
   }

   public void setPlaceholder(String placeholder) {
      this.placeholder = placeholder;
      if (!this.edit) {
         this.setPlaceholder();
      }

   }

   public String getPlaceholder() {
      return this.placeholder;
   }

   public void setText(String text) {
      this.setText((Object)text);
   }

   public void setText(Object text) {
      if (text != null && !text.equals(" ")) {
         this.onChangePrepare();
         String r = text != null ? text.toString() : "";
         this.value = this.check(r) ? r : null;
         super.setText(r);
      } else if (this.placeholder != null) {
         this.setPlaceholder();
      }

   }

   public void setValue(Object value) {
      this.setText(value);
   }

   public void setValue(String value) {
      this.setText(value);
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
         this.setBackground(this.parent.wrongColor);
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
         this.setBackground(this.parent.textBackground);
         this.setForeground(this.parent.textForeground);
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
         this.setForeground(this.parent.textForeground);
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

   public void updateLocale() {
      this.check();
   }

   protected abstract boolean check(String var1);
}
