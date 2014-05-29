package ru.turikhay.tlauncher.ui.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

public class ExtendedTextField extends JTextField {
   private static final long serialVersionUID = -1963422246993419362L;
   private CenterPanelTheme theme;
   private String placeholder;
   private String oldPlaceholder;

   protected ExtendedTextField(CenterPanel panel, String placeholder, String value) {
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
         this.setRawText(value);
      }

   }

   private void setPlaceholder() {
      this.setForeground(this.theme.getFocusLost());
      this.setRawText(this.placeholder);
   }

   private void setEmpty() {
      this.setForeground(this.theme.getFocus());
      this.setRawText("");
   }

   protected void updateStyle() {
      this.setForeground(this.getValue() == null ? this.theme.getFocusLost() : this.theme.getFocus());
   }

   public void setValue(Object obj) {
      this.setText(obj == null ? null : obj.toString());
   }

   protected void setValue(String s) {
      this.setText(s);
   }

   protected void setRawText(String s) {
      super.setText(s);
      super.setCaretPosition(0);
   }

   public String getPlaceholder() {
      return this.placeholder;
   }

   protected void setPlaceholder(String placeholder) {
      this.oldPlaceholder = this.placeholder;
      this.placeholder = placeholder;
      if (this.getValue() == null) {
         this.setPlaceholder();
      }

   }

   public CenterPanelTheme getTheme() {
      return this.theme;
   }

   protected void setTheme(CenterPanelTheme theme) {
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
