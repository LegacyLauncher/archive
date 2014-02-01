package com.turikhay.tlauncher.ui.text;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.center.CenterPanelTheme;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ExtendedTextField extends JTextField {
   private static final long serialVersionUID = -1963422246993419362L;
   private CenterPanelTheme theme;
   private String placeholder;
   private String oldPlaceholder;

   public ExtendedTextField(CenterPanel panel, String placeholder, String value) {
      this.theme = panel == null ? CenterPanel.defaultTheme : panel.getTheme();
      this.placeholder = placeholder;
      this.addFocusListener(new FocusListener() {
         public void focusGained(FocusEvent e) {
            ExtendedTextField.this.onFocusGained();
         }

         public void focusLost(FocusEvent e) {
            ExtendedTextField.this.onFocusLost();
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
      this.setValue(value);
   }

   public ExtendedTextField(String placeholder, String value) {
      this((CenterPanel)null, placeholder, value);
   }

   public ExtendedTextField(String placeholder) {
      this((CenterPanel)null, placeholder, (String)null);
   }

   /** @deprecated */
   @Deprecated
   public String getText() {
      return super.getText();
   }

   private String getValueOf(String value) {
      return value != null && !value.isEmpty() && !value.equals(this.placeholder) && !value.equals(this.oldPlaceholder) ? value : null;
   }

   public String getValue() {
      return this.getValueOf(this.getText());
   }

   public void setText(String text) {
      String value = this.getValueOf(text);
      if (value == null) {
         this.setPlaceholder();
      } else {
         this.setForeground(this.theme.getFocus());
         super.setText(value);
      }

   }

   private void setPlaceholder() {
      this.setForeground(this.theme.getFocusLost());
      super.setText(this.placeholder);
   }

   private void setEmpty() {
      this.setForeground(this.theme.getFocus());
      super.setText("");
   }

   protected void updateStyle() {
      this.setForeground(this.getValue() == null ? this.theme.getFocusLost() : this.theme.getFocus());
   }

   public void setValue(Object obj) {
      this.setText(obj == null ? null : obj.toString());
   }

   public void setValue(String s) {
      this.setText(s);
   }

   public String getPlaceholder() {
      return this.placeholder;
   }

   public void setPlaceholder(String placeholder) {
      this.oldPlaceholder = this.placeholder;
      this.placeholder = placeholder;
      if (this.getValue() == null) {
         this.setPlaceholder();
      }

   }

   public CenterPanelTheme getTheme() {
      return this.theme;
   }

   public void setTheme(CenterPanelTheme theme) {
      if (theme == null) {
         theme = CenterPanel.defaultTheme;
      }

      this.theme = theme;
      this.updateStyle();
   }

   protected void onFocusGained() {
      if (this.getValue() == null) {
         this.setEmpty();
      }

   }

   protected void onFocusLost() {
      if (this.getValue() == null) {
         this.setPlaceholder();
      }

   }

   protected void onChange() {
   }
}
