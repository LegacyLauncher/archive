package ru.turikhay.tlauncher.ui.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class DocumentChangeListener implements DocumentListener {
   public void insertUpdate(DocumentEvent e) {
      this.documentChanged(e);
   }

   public void removeUpdate(DocumentEvent e) {
      this.documentChanged(e);
   }

   public void changedUpdate(DocumentEvent e) {
      this.documentChanged(e);
   }

   public abstract void documentChanged(DocumentEvent var1);
}
