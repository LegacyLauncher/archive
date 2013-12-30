package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.TextListener;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class ExtendedTextField extends JTextField implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
   protected CenterPanel parent;
   protected Settings l;
   protected String value;
   protected String placeholder;
   protected boolean edit;
   private String error;
   public static final Font PLACEHOLDER_FONT = new Font("", 2, 12);
   public static final Font DEFAULT_FONT = new Font("", 0, 12);
   public static final Color OK_BACKGROUND;
   public static final Color OK_FOREGROUND;
   public static final Color WRONG_BACKGROUND;
   public static final Color WRONG_FOREGROUND;
   protected Font placeholder_font;
   protected Font default_font;
   protected Color ok_background;
   protected Color ok_foreground;
   protected Color wrong_background;
   protected Color wrong_foreground;
   protected FocusListener focusListener;
   protected TextListener textListener;

   static {
      OK_BACKGROUND = Color.white;
      OK_FOREGROUND = Color.black;
      WRONG_BACKGROUND = Color.pink;
      WRONG_FOREGROUND = Color.black;
   }

   protected ExtendedTextField() {
      this.placeholder_font = new Font("", 2, 12);
      this.default_font = new Font("", 0, 12);
      this.ok_background = Color.white;
      this.ok_foreground = Color.black;
      this.wrong_background = Color.pink;
      this.wrong_foreground = Color.black;
      this.initListeners();
   }

   public ExtendedTextField(CenterPanel parentPanel, String placeholder, String text, int columns) {
      super(columns);
      this.placeholder_font = new Font("", 2, 12);
      this.default_font = new Font("", 0, 12);
      this.ok_background = Color.white;
      this.ok_foreground = Color.black;
      this.wrong_background = Color.pink;
      this.wrong_foreground = Color.black;
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
      this.setBackground(this.parent == null ? this.ok_background : this.parent.textBackground);
      this.setForeground(this.parent == null ? this.ok_foreground : this.parent.panelColor);
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
         String r = text.toString();
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
      this.setBackground(this.parent == null ? this.wrong_background : this.parent.wrongColor);
      this.setForeground(this.parent == null ? this.wrong_foreground : this.parent.textForeground);
      if (this.parent != null && reason != null) {
         this.parent.setError(reason);
      }

      return false;
   }

   protected boolean ok() {
      this.setBackground(this.parent == null ? this.ok_background : this.parent.textBackground);
      this.setForeground(this.parent == null ? this.ok_foreground : this.parent.textForeground);
      this.setFont(this.parent == null ? this.default_font : this.parent.font);
      if (this.parent != null) {
         this.parent.setError((String)null);
      }

      return true;
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

   protected void onChange() {
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
         this.setFont(this.parent == null ? this.default_font : this.parent.font);
         this.setForeground(this.parent == null ? this.ok_foreground : this.parent.textForeground);
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
      this.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent e) {
            ExtendedTextField.this.onChange();
         }

         public void removeUpdate(DocumentEvent e) {
            ExtendedTextField.this.onChange();
         }

         public void changedUpdate(DocumentEvent e) {
            ExtendedTextField.this.onChange();
         }
      });
   }

   public void updateLocale() {
      this.check();
   }

   protected abstract boolean check(String var1);
}
