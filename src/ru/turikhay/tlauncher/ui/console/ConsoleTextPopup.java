package ru.turikhay.tlauncher.ui.console;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.ExtensionFileFilter;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.EmptyAction;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.stream.StringStream;

public class ConsoleTextPopup extends TextPopup {
   private final Console console;
   private final FileExplorer explorer;
   private final Action saveAllAction;

   ConsoleTextPopup(Console console) {
      this.console = console;
      this.explorer = new FileExplorer();
      this.explorer.setFileFilter(new ExtensionFileFilter("log"));
      this.saveAllAction = new EmptyAction() {
         public void actionPerformed(ActionEvent e) {
            ConsoleTextPopup.this.onSavingCalled(e);
         }
      };
   }

   protected JPopupMenu getPopup(MouseEvent e, JTextComponent comp) {
      JPopupMenu menu = super.getPopup(e, comp);
      if (menu == null) {
         return null;
      } else {
         menu.addSeparator();
         menu.add(this.saveAllAction).setText(Localizable.get("console.save.popup"));
         return menu;
      }
   }

   protected void onSavingCalled(ActionEvent e) {
      this.explorer.setSelectedFile(new File(this.console.getName() + ".log"));
      int result = this.explorer.showSaveDialog(this.console.frame);
      if (result == 0) {
         File file = this.explorer.getSelectedFile();
         if (file == null) {
            U.log("Returned NULL. Damn it!");
         } else {
            String path = file.getAbsolutePath();
            if (!path.endsWith(".log")) {
               path = path + ".log";
            }

            file = new File(path);
            BufferedOutputStream output = null;

            try {
               FileUtil.createFile(file);
               StringStream input = this.console.getStream();
               output = new BufferedOutputStream(new FileOutputStream(file));
               boolean addR = OS.WINDOWS.isCurrent();
               int caret = -1;

               while(true) {
                  ++caret;
                  if (caret >= input.getLength()) {
                     output.close();
                     break;
                  }

                  char current = input.getCharAt(caret);
                  if (current == '\n' && addR) {
                     output.write(13);
                  }

                  output.write(current);
               }
            } catch (Throwable var18) {
               Alert.showLocError("console.save.error", var18);
            } finally {
               if (output != null) {
                  try {
                     output.close();
                  } catch (IOException var17) {
                     var17.printStackTrace();
                  }
               }

            }

         }
      }
   }
}
