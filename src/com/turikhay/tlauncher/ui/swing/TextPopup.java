package com.turikhay.tlauncher.ui.swing;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
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
   static LangConfiguration l;

   public TextPopup() {
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
         if (enabled && editable && marked) {
            Action cutAction = textComponent.getActionMap().get("cut-to-clipboard");
            if (cutAction == null) {
               cutAction = textComponent.getActionMap().get("cut");
            }

            if (cutAction != null) {
               popup.add(cutAction).setText(CUT);
            }
         }

         if (enabled && marked) {
            Action copyAction = textComponent.getActionMap().get("copy-to-clipboard");
            if (copyAction == null) {
               copyAction = textComponent.getActionMap().get("copy");
            }

            if (copyAction != null) {
               popup.add(copyAction).setText(COPY);
            }
         }

         if (enabled && editable && pasteAvailable) {
            Action pasteAction = textComponent.getActionMap().get("paste-from-clipboard");
            if (pasteAction == null) {
               pasteAction = textComponent.getActionMap().get("paste");
            }

            if (pasteAction != null) {
               popup.add(pasteAction).setText(PASTE);
            }
         }

         if (enabled && nonempty) {
            Action selectAllAction = textComponent.getActionMap().get("select-all");
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
      if (l == null && TLauncher.getInstance() != null) {
         l = Localizable.get();
      }

      if (l == null) {
         CUT = "Cut";
         COPY = "Copy";
         SELECTALL = "Select all";
         PASTE = "Paste";
      } else {
         CUT = l.get("popup.cut");
         COPY = l.get("popup.copy");
         SELECTALL = l.get("popup.selectall");
         PASTE = l.get("popup.paste");
      }

   }
}
