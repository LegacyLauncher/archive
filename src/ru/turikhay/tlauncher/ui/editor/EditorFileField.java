package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.U;

public class EditorFileField extends BorderPanel implements EditorField {
   private final EditorTextField textField;
   private final LocalizableButton explorerButton;
   private final FileExplorer explorer;
   private boolean permitUrl;

   public EditorFileField(String prompt, String buttonPath, FileExplorer exp, boolean canBeEmpty, boolean permitUrl) {
      this.permitUrl = permitUrl;
      this.textField = new EditorTextField(prompt, canBeEmpty);
      this.explorerButton = new LocalizableButton(buttonPath);
      this.explorerButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (EditorFileField.this.explorer != null) {
               EditorFileField.this.explorerButton.setEnabled(false);
               EditorFileField.this.explorer.setCurrentDirectory(EditorFileField.this.getSelectedFile());
               int result = EditorFileField.this.explorer.showDialog(EditorFileField.this);
               if (result == 0) {
                  File selected = EditorFileField.this.explorer.getSelectedFile();
                  String path;
                  if (selected == null) {
                     path = "";
                  } else {
                     try {
                        path = selected.getCanonicalPath();
                     } catch (Exception var6) {
                        path = selected.getAbsolutePath();
                        EditorFileField.this.log(var6);
                     }
                  }

                  EditorFileField.this.setSettingsValue(path);
               }

               EditorFileField.this.explorerButton.setEnabled(true);
            }
         }
      });
      this.explorer = exp;
      this.setCenter(this.textField);
      if (this.explorer != null) {
         this.setEast(this.explorerButton);
      }

   }

   public EditorFileField(String prompt, FileExplorer exp, boolean canBeEmpty, boolean permitUrl) {
      this(prompt, "explorer.browse", exp, canBeEmpty, permitUrl);
   }

   public File getSelectedFile() {
      File selected = null;
      if (this.getSettingsValue() != null && parseUrl(this.getSettingsValue()) == null) {
         selected = new File(this.getSettingsValue());
      } else if (this.explorer != null) {
         selected = this.explorer.getSelectedFile();
      }

      return selected == null ? TLauncher.getDirectory() : selected;
   }

   public void setBackground(Color bg) {
      if (this.textField != null) {
         this.textField.setBackground(bg);
      }

   }

   public String getSettingsValue() {
      String value = this.textField.getSettingsValue();

      URL testUrl;
      try {
         testUrl = new URL(value);
      } catch (Exception var4) {
         return value;
      }

      return testUrl.toString();
   }

   public void setSettingsValue(String var) {
      this.textField.setSettingsValue(var);
   }

   public boolean isValueValid() {
      return !this.permitUrl && parseUrl(this.getSettingsValue()) != null ? false : this.textField.isValueValid();
   }

   public void block(Object reason) {
      Blocker.blockComponents(reason, this.textField, this.explorerButton);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(Blocker.UNIVERSAL_UNBLOCK, this.textField, this.explorerButton);
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }

   private static URL parseUrl(String s) {
      try {
         URL testUrl = new URL(s);
         return testUrl;
      } catch (Exception var3) {
         return null;
      }
   }
}
