package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.regex.Pattern;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.U;

public class EditorFileField extends BorderPanel implements EditorField {
   public static final char DEFAULT_DELIMITER = ';';
   private final EditorTextField textField;
   private final LocalizableButton explorerButton;
   private final FileExplorer explorer;
   private final char delimiterChar;
   private final Pattern delimiterSplitter;

   public EditorFileField(String prompt, boolean canBeEmpty, String button, FileExplorer chooser, char delimiter) {
      this.textField = new EditorTextField(prompt, canBeEmpty);
      this.explorerButton = new LocalizableButton(button);
      this.explorer = chooser;
      this.delimiterChar = delimiter;
      this.delimiterSplitter = Pattern.compile(String.valueOf(this.delimiterChar), 16);
      this.explorerButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (EditorFileField.this.explorer != null) {
               EditorFileField.this.explorerButton.setEnabled(false);
               EditorFileField.this.explorer.setCurrentDirectory(EditorFileField.this.getFirstFile());
               int result = EditorFileField.this.explorer.showDialog(EditorFileField.this);
               if (result == 0) {
                  EditorFileField.this.setRawValue(EditorFileField.this.explorer.getSelectedFiles());
               }

               EditorFileField.this.explorerButton.setEnabled(true);
            }

         }
      });
      this.add(this.textField, "Center");
      if (this.explorer != null) {
         this.add(this.explorerButton, "East");
      }

   }

   public EditorFileField(String prompt, boolean canBeEmpty, FileExplorer chooser) {
      this(prompt, canBeEmpty, "explorer.browse", chooser, ';');
   }

   public EditorFileField(String prompt, FileExplorer chooser) {
      this(prompt, false, chooser);
   }

   public String getSettingsValue() {
      return this.getValueFromRaw(this.getRawValues());
   }

   private File[] getRawValues() {
      String[] paths = this.getRawSplitValue();
      if (paths == null) {
         return null;
      } else {
         int len = paths.length;
         File[] files = new File[len];

         for(int i = 0; i < paths.length; ++i) {
            files[i] = new File(paths[i]);
         }

         return files;
      }
   }

   public void setSettingsValue(String value) {
      this.textField.setSettingsValue(value);
   }

   private void setRawValue(File[] fileList) {
      this.setSettingsValue(this.getValueFromRaw(fileList));
   }

   private String[] getRawSplitValue() {
      return this.splitString(this.textField.getValue());
   }

   private String getValueFromRaw(File[] files) {
      if (files == null) {
         return null;
      } else {
         StringBuilder builder = new StringBuilder();
         File[] var6 = files;
         int var5 = files.length;

         for(int var4 = 0; var4 < var5; ++var4) {
            File file = var6[var4];
            String path = file.getAbsolutePath();
            builder.append(this.delimiterChar).append(path);
         }

         return builder.substring(1);
      }
   }

   private String[] splitString(String s) {
      if (s == null) {
         return null;
      } else {
         String[] split = this.delimiterSplitter.split(s);
         return split.length == 0 ? null : split;
      }
   }

   private File getFirstFile() {
      File[] files = this.getRawValues();
      return files != null && files.length != 0 ? files[0] : TLauncher.getDirectory();
   }

   public boolean isValueValid() {
      return this.textField.isValueValid();
   }

   public void setBackground(Color bg) {
      if (this.textField != null) {
         this.textField.setBackground(bg);
      }

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
}
