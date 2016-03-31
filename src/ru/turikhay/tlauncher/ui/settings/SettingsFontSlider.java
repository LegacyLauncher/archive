package ru.turikhay.tlauncher.ui.settings;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JSlider;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.editor.EditorIntegerField;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class SettingsFontSlider extends BorderPanel implements EditorField {
   private final JSlider slider = new JSlider();
   private EditorIntegerField inputField;
   private final LocalizableLabel pt;

   SettingsFontSlider() {
      this.slider.setOpaque(false);
      this.slider.setMinimum(12);
      this.slider.setMaximum(18);
      this.slider.setMinorTickSpacing(2);
      this.slider.setMajorTickSpacing(4);
      this.slider.setSnapToTicks(true);
      this.slider.setPaintTicks(true);
      this.slider.addMouseListener(new MouseAdapter() {
         public void mouseReleased(MouseEvent e) {
            SettingsFontSlider.this.requestFocusInWindow();
         }
      });
      this.setCenter(this.slider);
      this.inputField = new EditorIntegerField();
      this.inputField.textField.setColumns(2);
      this.pt = new LocalizableLabel("settings.fontsize.pt");
      ExtendedPanel panel = new ExtendedPanel();
      panel.add(this.inputField, this.pt);
      this.setEast(panel);
      this.slider.addMouseListener(new MouseAdapter() {
         public void mouseReleased(MouseEvent e) {
            SettingsFontSlider.this.onSliderUpdate();
         }
      });
      this.slider.addKeyListener(new KeyAdapter() {
         public void keyReleased(KeyEvent e) {
            SettingsFontSlider.this.onSliderUpdate();
         }
      });
      this.inputField.textField.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent e) {
            SettingsFontSlider.this.updateInfo();
         }

         public void removeUpdate(DocumentEvent e) {
         }

         public void changedUpdate(DocumentEvent e) {
         }
      });
   }

   public void setBackground(Color color) {
      if (this.inputField != null) {
         this.inputField.setBackground(color);
      }

   }

   public void block(Object reason) {
      Blocker.blockComponents(reason, this.slider, this.inputField);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(reason, this.slider, this.inputField);
   }

   public String getSettingsValue() {
      return this.inputField.textField.getValue();
   }

   public void setSettingsValue(String value) {
      this.inputField.textField.setValue(value);
      this.updateInfo();
   }

   public boolean isValueValid() {
      return this.inputField.getIntegerValue() >= 12;
   }

   private void onSliderUpdate() {
      this.inputField.textField.setValue(this.slider.getValue());
   }

   private void updateSlider() {
      int intVal = this.inputField.getIntegerValue();
      if (intVal > 1) {
         this.slider.setValue(intVal);
      }

   }

   private void updateInfo() {
      this.updateSlider();
   }
}
