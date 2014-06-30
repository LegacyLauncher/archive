package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;

public abstract class AbstractEditorPanel extends CenterPanel {
   protected final List handlers;

   public AbstractEditorPanel(CenterPanelTheme theme, Insets insets) {
      super(theme, insets);
      this.handlers = new ArrayList();
   }

   public AbstractEditorPanel(Insets insets) {
      this((CenterPanelTheme)null, insets);
   }

   public AbstractEditorPanel() {
      this((CenterPanelTheme)null, (Insets)null);
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
      handler.getComponent().setBackground(color);
   }

   protected JComponent createTip(String label, boolean warning) {
      LocalizableLabel tip = new LocalizableLabel(label);
      if (warning) {
         ImageIcon.setup(tip, ImageCache.getIcon("warning.png", 16, 16));
      }

      return tip;
   }
}
