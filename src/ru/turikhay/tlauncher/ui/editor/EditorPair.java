package ru.turikhay.tlauncher.ui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class EditorPair {
   private final LocalizableLabel label;
   private final List handlers;
   private final ExtendedPanel panel;
   public static final EditorHandler NEXT_COLUMN = new EditorPair.NextColumn();

   public EditorPair(String labelPath, List handlers) {
      this.handlers = handlers;
      this.label = new LocalizableLabel(labelPath);
      this.panel = new ExtendedPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = 1;
      c.weightx = 1.0D;
      c.gridy = -1;
      EditorHandler prev = null;
      Iterator var5 = handlers.iterator();

      while(var5.hasNext()) {
         EditorHandler handler = (EditorHandler)var5.next();
         if (NEXT_COLUMN.equals(handler)) {
            ++c.gridx;
            prev = handler;
         } else {
            if (!NEXT_COLUMN.equals(prev)) {
               c.gridx = 0;
               ++c.gridy;
            }

            JComponent comp = handler.getComponent();
            comp.setAlignmentX(0.0F);
            this.panel.add(comp, c);
            prev = handler;
         }
      }

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

   public ExtendedPanel getPanel() {
      return this.panel;
   }

   private static class NextColumn extends EditorHandler {
      private NextColumn() {
         super((String)null);
      }

      public boolean isValid() {
         return true;
      }

      public JComponent getComponent() {
         return null;
      }

      public String getValue() {
         return null;
      }

      protected void setValue0(String var1) {
      }

      public void block(Object var1) {
      }

      public void unblock(Object var1) {
      }

      // $FF: synthetic method
      NextColumn(Object x0) {
         this();
      }
   }
}
