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
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;

public class SettingsMemorySlider extends BorderPanel implements EditorField {
   private final JSlider slider = new JSlider();
   private final EditorIntegerField inputField;
   private final LocalizableLabel mb;
   private final LocalizableLabel hint;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$util$OS$Arch;

   SettingsMemorySlider() {
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
      this.inputField.setColumns(5);
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
      this.inputField.getDocument().addDocumentListener(new DocumentListener() {
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
         this.inputField.setBackground(color);
      }

   }

   public void block(Object reason) {
      Blocker.blockComponents(reason, this.slider, this.inputField, this.hint);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(reason, this.slider, this.inputField, this.hint);
   }

   public String getSettingsValue() {
      return this.inputField.getValue();
   }

   public void setSettingsValue(String value) {
      this.inputField.setValue(value);
      this.updateInfo();
   }

   public boolean isValueValid() {
      return this.inputField.getIntegerValue() >= 512;
   }

   private void onSliderUpdate() {
      this.inputField.setValue(this.slider.getValue());
      this.updateTip();
   }

   private void updateSlider() {
      int intVal = this.inputField.getIntegerValue();
      if (intVal > 1) {
         this.slider.setValue(intVal);
      }

   }

   private void updateTip() {
      int intVal = this.inputField.getIntegerValue();
      SettingsMemorySlider.ValueType value = null;
      if (intVal < 512) {
         value = SettingsMemorySlider.ValueType.DANGER;
      } else if (intVal == OS.Arch.PREFERRED_MEMORY) {
         value = SettingsMemorySlider.ValueType.OK;
      } else {
         switch($SWITCH_TABLE$ru$turikhay$util$OS$Arch()[OS.Arch.CURRENT.ordinal()]) {
         case 2:
            if (OS.Arch.TOTAL_RAM_MB > 0L && (long)intVal > OS.Arch.TOTAL_RAM_MB) {
               value = SettingsMemorySlider.ValueType.DANGER;
            } else if (intVal > OS.Arch.MAX_MEMORY) {
               value = SettingsMemorySlider.ValueType.WARNING;
            }
            break;
         default:
            if (intVal > OS.Arch.MAX_MEMORY) {
               value = SettingsMemorySlider.ValueType.DANGER;
            } else if (intVal > OS.Arch.PREFERRED_MEMORY) {
               value = SettingsMemorySlider.ValueType.WARNING;
            }
         }
      }

      String path;
      ImageIcon icon;
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

   private void updateInfo() {
      this.updateSlider();
      this.updateTip();
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$util$OS$Arch() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$util$OS$Arch;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[OS.Arch.values().length];

         try {
            var0[OS.Arch.UNKNOWN.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[OS.Arch.x32.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[OS.Arch.x64.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$util$OS$Arch = var0;
         return var0;
      }
   }

   private static enum ValueType {
      OK("info.png"),
      WARNING("warning.png"),
      DANGER("danger.png");

      private final String path = "settings.java.memory.hint." + this.toString().toLowerCase();
      private final ImageIcon icon;

      private ValueType(String image) {
         this.icon = ImageCache.getIcon(image, 16, 16);
      }
   }
}
