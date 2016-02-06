package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

public abstract class AbstractEditorPanel extends CenterPanel {
   protected final List handlers = new ArrayList();

   public AbstractEditorPanel(CenterPanelTheme theme, Insets insets) {
      super(theme, insets);
   }

   protected boolean checkValues() {
      boolean allValid = true;
      Iterator var3 = this.handlers.iterator();

      while(var3.hasNext()) {
         EditorHandler handler = (EditorHandler)var3.next();
         boolean valid = handler.isValid();
         this.setValid(handler, valid);
         if (!valid) {
            allValid = false;
         }
      }

      return allValid;
   }

   protected void setValid(EditorHandler handler, boolean valid) {
      Color color = valid ? this.getTheme().getBackground() : this.getTheme().getFailure();
      if (handler.getComponent() != null) {
         handler.getComponent().setOpaque(!valid);
         handler.getComponent().setBackground(color);
      }

   }
}
