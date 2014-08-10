package ru.turikhay.tlauncher.ui.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

public class ExtendedPasswordField extends JPasswordField {
   private static final long serialVersionUID = 3175896797135831502L;
   private static final String DEFAULT_PLACEHOLDER = "пассворд, лол";
   private CenterPanelTheme theme;
   private String placeholder;

   private ExtendedPasswordField(CenterPanel panel, String placeholder) {
      this.theme = panel == null ? CenterPanel.defaultTheme : panel.getTheme();
      this.placeholder = placeholder == null ? "пассворд, лол" : placeholder;
      this.addFocusListener(new FocusListener() {
         public void focusGained(FocusEvent e) {
            ExtendedPasswordField.this.onFocusGained();
         }

         public void focusLost(FocusEvent e) {
            ExtendedPasswordField.this.onFocusLost();
         }
      });
      this.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent e) {
            ExtendedPasswordField.this.onChange();
         }

         public void removeUpdate(DocumentEvent e) {
            ExtendedPasswordField.this.onChange();
         }

         public void changedUpdate(DocumentEvent e) {
            ExtendedPasswordField.this.onChange();
         }
      });
      this.setText((String)null);
   }

   public ExtendedPasswordField() {
      this((CenterPanel)null, (String)null);
   }

   private String getValueOf(String value) {
      return value != null && !value.isEmpty() && !value.equals(this.placeholder) ? value : null;
   }

   /** @deprecated */
   @Deprecated
   public String getText() {
      return super.getText();
   }

   public char[] getPassword() {
      String value = this.getValue();
      return value == null ? new char[0] : value.toCharArray();
   }

   public boolean hasPassword() {
      return this.getValue() != null;
   }

   private String getValue() {
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

   void updateStyle() {
      this.setForeground(this.getValue() == null ? this.theme.getFocusLost() : this.theme.getFocus());
   }

   public String getPlaceholder() {
      return this.placeholder;
   }

   public void setPlaceholder(String placeholder) {
      this.placeholder = placeholder == null ? "пассворд, лол" : placeholder;
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
