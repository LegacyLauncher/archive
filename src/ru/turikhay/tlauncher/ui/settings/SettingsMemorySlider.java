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
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

public class SettingsMemorySlider extends BorderPanel implements EditorField {
   private final SettingsPanel settings;
   private final JSlider slider = new JSlider();
   private EditorIntegerField inputField;
   private final LocalizableLabel mb;
   private final LocalizableLabel hint;

   SettingsMemorySlider(SettingsPanel s) {
      this.settings = s;
      this.slider.setOpaque(false);
      this.slider.setMinimum(512);
      this.slider.setMaximum(OS.Arch.MAX_MEMORY);
      this.slider.setMinorTickSpacing(OS.Arch.x64.isCurrent() ? 256 : 128);
      this.slider.setMajorTickSpacing(512);
      this.slider.setSnapToTicks(true);
      this.slider.setPaintLabels(true);
      this.slider.setPaintTicks(true);
      this.slider.addMouseListener(new MouseAdapter() {
         public void mouseReleased(MouseEvent e) {
            SettingsMemorySlider.this.requestFocusInWindow();
         }
      });
      this.setCenter(this.slider);
      this.inputField = new EditorIntegerField();
      this.inputField.textField.setColumns(5);
      this.mb = new LocalizableLabel("settings.java.memory.mb");
      ExtendedPanel panel = new ExtendedPanel();
      panel.add(this.inputField, this.mb);
      this.setEast(panel);
      this.hint = new LocalizableHTMLLabel("");
      this.setSouth(this.hint);
      this.slider.addMouseListener(new MouseAdapter() {
         public void mouseReleased(MouseEvent e) {
            SettingsMemorySlider.this.onSliderUpdate();
         }
      });
      this.slider.addKeyListener(new KeyAdapter() {
         public void keyReleased(KeyEvent e) {
            SettingsMemorySlider.this.onSliderUpdate();
         }
      });
      this.inputField.textField.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent e) {
            SettingsMemorySlider.this.updateInfo();
         }

         public void removeUpdate(DocumentEvent e) {
         }

         public void changedUpdate(DocumentEvent e) {
         }
      });
   }

   public void setBackground(Color color) {
      if (this.inputField != null) {
         this.inputField.textField.setBackground(color);
      }

   }

   public void block(Object reason) {
      Blocker.blockComponents(reason, this.slider, this.inputField, this.hint);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(reason, this.slider, this.inputField, this.hint);
   }

   public String getSettingsValue() {
      return this.inputField.textField.getValue();
   }

   public void setSettingsValue(String value) {
      this.inputField.textField.setValue(value);
      this.updateInfo();
   }

   public boolean isValueValid() {
      return this.inputField.getIntegerValue() >= 512;
   }

   private void onSliderUpdate() {
      this.inputField.textField.setValue(this.slider.getValue());
      this.updateTip();
   }

   private void updateSlider() {
      int intVal = this.inputField.getIntegerValue();
      if (intVal > 1) {
         this.slider.setValue(intVal);
      }

   }

   private void updateTip() {
      SettingsMemorySlider.ValueType value = null;
      if (this.settings.cmd.getValue() == null) {
         int intVal = this.inputField.getIntegerValue();
         if (intVal < 512) {
            value = SettingsMemorySlider.ValueType.DANGER;
         } else if (intVal == OS.Arch.PREFERRED_MEMORY) {
            value = SettingsMemorySlider.ValueType.OK;
         } else {
            switch(OS.Arch.CURRENT) {
            case x32:
               if (OS.Arch.TOTAL_RAM_MB > 0L && (long)intVal > OS.Arch.TOTAL_RAM_MB) {
                  value = SettingsMemorySlider.ValueType.DANGER;
               } else if (intVal > OS.Arch.MAX_MEMORY) {
                  value = SettingsMemorySlider.ValueType.WARNING;
               }
               break;
            default:
               if ((long)intVal > OS.Arch.TOTAL_RAM_MB) {
                  value = SettingsMemorySlider.ValueType.DANGER;
               }
            }
         }
      } else {
         value = null;
      }

      ImageIcon icon;
      String path;
      if (value == null) {
         path = "";
         icon = null;
      } else {
         path = value.path;
         icon = value.icon;
      }

      this.hint.setText(path);
      ImageIcon.setup(this.hint, icon);
   }

   public void updateInfo() {
      this.updateSlider();
      this.updateTip();
   }

   private static enum ValueType {
      OK("info.png"),
      WARNING("warning.png"),
      DANGER("danger.png");

      private final String path = "settings.java.memory.hint." + this.toString().toLowerCase();
      private final ImageIcon icon;

      private ValueType(String image) {
         this.icon = Images.getIcon(image, SwingUtil.magnify(16), SwingUtil.magnify(16));
      }
   }
}
