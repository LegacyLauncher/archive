package ru.turikhay.tlauncher.ui.settings;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.converter.LocaleConverter;
import ru.turikhay.tlauncher.ui.editor.EditorComboBox;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.OS;

public class SettingsLocaleComboBox extends BorderPanel implements EditorField, LocalizableComponent {
   final SettingsPanel panel;
   final EditorComboBox comboBox;
   final ExtendedLabel hint;

   public SettingsLocaleComboBox(SettingsPanel panel) {
      this.panel = panel;
      this.comboBox = new EditorComboBox(new LocaleConverter(), panel.global.getLocales());
      this.setCenter(this.comboBox);
      this.hint = new ExtendedLabel();
      this.hint.setFont(this.hint.getFont().deriveFont((float)this.hint.getFont().getSize() - 2.0F));
      this.hint.setCursor(Cursor.getPredefinedCursor(12));
      this.hint.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            OS.openLink("http://tlaun.ch/l10n?locale=" + SettingsLocaleComboBox.this.panel.global.getLocale());
         }
      });
      this.setSouth(this.hint);
      this.updateLocale();
   }

   public String getSettingsValue() {
      return this.comboBox.getSettingsValue();
   }

   public void setSettingsValue(String var1) {
      this.comboBox.setSettingsValue(var1);
   }

   public boolean isValueValid() {
      return this.comboBox.isValueValid();
   }

   public void block(Object var1) {
      this.comboBox.setEnabled(false);
      this.hint.setEnabled(false);
   }

   public void unblock(Object var1) {
      this.comboBox.setEnabled(true);
      this.hint.setEnabled(true);
   }

   public void updateLocale() {
      String locale = this.panel.lang.getSelected().toString();
      this.hint.setVisible(!locale.equals("ru_RU") && !locale.equals("uk_UA"));
      String hintLocalized = this.panel.lang.nget("settings.lang.contribute");
      if (StringUtils.isEmpty(hintLocalized)) {
         hintLocalized = this.panel.lang.getDefault("settings.lang.contribute");
      }

      this.hint.setText("<html>" + hintLocalized + "</html>");
   }
}
