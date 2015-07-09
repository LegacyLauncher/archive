package ru.turikhay.tlauncher.ui.editor;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;

public class EditorPair {
   private final LocalizableLabel label;
   private final List handlers;
   private final JComponent[] fields;
   private final VPanel panel;

   public EditorPair(String labelPath, List handlers) {
      this.label = new LocalizableLabel(labelPath);
      int num = handlers.size();
      this.fields = new JComponent[num];

      for(int i = 0; i < num; ++i) {
         this.fields[i] = ((EditorHandler)handlers.get(i)).getComponent();
         this.fields[i].setAlignmentX(0.0F);
      }

      this.handlers = handlers;
      this.panel = new VPanel();
      this.panel.add(this.fields);
   }

   public EditorPair(String labelPath, EditorHandler... handlers) {
      this(labelPath, Arrays.asList(handlers));
   }

   public List getHandlers() {
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
