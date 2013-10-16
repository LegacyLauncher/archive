package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.GlobalSettings;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.text.JTextComponent;

public class TextPopup extends MouseAdapter implements LocalizableComponent {
   private static String SOURCE_CODE = "http://cloud-notes.blogspot.ru/2013/04/jtextcomponent-java.html";
   private static String CUT;
   private static String COPY;
   private static String SELECTALL;
   private static String PASTE;
   private GlobalSettings s;

   public TextPopup(GlobalSettings s) {
      this.s = s;
      this.updateLocale();
   }

   public void mouseClicked(MouseEvent e) {
      if (e.getModifiers() == 4) {
         if (!(e.getSource() instanceof JTextComponent)) {
            return;
         }

         JTextComponent textComponent = (JTextComponent)e.getSource();
         textComponent.requestFocus();
         boolean enabled = textComponent.isEnabled();
         boolean editable = textComponent.isEditable();
         boolean nonempty = textComponent.getText() != null && !textComponent.getText().equals("");
         boolean marked = textComponent.getSelectedText() != null;
         boolean pasteAvailable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object)null).isDataFlavorSupported(DataFlavor.stringFlavor);
         JPopupMenu popup = new JPopupMenu();
         Action selectAllAction;
         if (enabled && editable && marked) {
            selectAllAction = textComponent.getActionMap().get("cut-to-clipboard");
            if (selectAllAction == null) {
               selectAllAction = textComponent.getActionMap().get("cut");
            }

            if (selectAllAction != null) {
               popup.add(selectAllAction).setText(CUT);
            }
         }

         if (enabled && marked) {
            selectAllAction = textComponent.getActionMap().get("copy-to-clipboard");
            if (selectAllAction == null) {
               selectAllAction = textComponent.getActionMap().get("copy");
            }

            if (selectAllAction != null) {
               popup.add(selectAllAction).setText(COPY);
            }
         }

         if (enabled && editable && pasteAvailable) {
            selectAllAction = textComponent.getActionMap().get("paste-from-clipboard");
            if (selectAllAction == null) {
               selectAllAction = textComponent.getActionMap().get("paste");
            }

            if (selectAllAction != null) {
               popup.add(selectAllAction).setText(PASTE);
            }
         }

         if (enabled && nonempty) {
            selectAllAction = textComponent.getActionMap().get("select-all");
            if (selectAllAction == null) {
               selectAllAction = textComponent.getActionMap().get("selectAll");
            }

            if (selectAllAction != null) {
               if (popup.getComponentCount() > 0 && !(popup.getComponent(popup.getComponentCount() - 1) instanceof Separator)) {
                  popup.addSeparator();
               }

               popup.add(selectAllAction).setText(SELECTALL);
            }
         }

         if (popup.getComponentCount() > 0) {
            if (popup.getComponent(0) instanceof Separator) {
               popup.remove(0);
            }

            if (popup.getComponent(popup.getComponentCount() - 1) instanceof Separator) {
               popup.remove(popup.getComponentCount() - 1);
            }

            popup.show(e.getComponent(), e.getX(), e.getY() - popup.getSize().height);
         }
      }

   }

   public void updateLocale() {
      if (this.s == null) {
         CUT = "Cut";
         COPY = "Copy";
         SELECTALL = "Select all";
         PASTE = "Paste";
      } else {
         CUT = this.s.get("popup.cut");
         COPY = this.s.get("popup.copy");
         SELECTALL = this.s.get("popup.selectall");
         PASTE = this.s.get("popup.paste");
      }

   }
}
