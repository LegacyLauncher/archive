package ru.turikhay.tlauncher.ui.editor;

import java.awt.Component;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;

public class EditorPair {
   private final LocalizableLabel label;
   private final EditorHandler[] handlers;
   private final JComponent[] fields;
   private final VPanel panel;

   public EditorPair(String labelPath, EditorHandler... handlers) {
      this.label = new LocalizableLabel(labelPath);
      int num = handlers.length;
      this.fields = new JComponent[num];

      for(int i = 0; i < num; ++i) {
         this.fields[i] = handlers[i].getComponent();
         this.fields[i].setAlignmentX(0.0F);
      }

      this.handlers = handlers;
      this.panel = new VPanel();
      this.panel.add(this.fields);
   }

   public EditorHandler[] getHandlers() {
      return this.handlers;
   }

   public LocalizableLabel getLabel() {
      return this.label;
   }

   public Component[] getFields() {
      return this.fields;
   }

   public VPanel getPanel() {
      return this.panel;
   }
}
