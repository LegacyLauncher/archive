package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class EditorPanel extends CenterPanel {
   private static final long serialVersionUID = 3428243378644563729L;
   protected final ExtendedPanel container;
   protected final ScrollPane scroll;
   private final List panels;
   private final List constraints;
   protected final List handlers;
   private byte paneNum;
   private byte rowNum;

   public EditorPanel(Insets insets) {
      super(insets);
      this.container = new ExtendedPanel();
      this.container.setLayout(new BoxLayout(this.container, 3));
      this.panels = new ArrayList();
      this.constraints = new ArrayList();
      this.handlers = new ArrayList();
      this.scroll = new ScrollPane(this.container);
      this.add(this.messagePanel, this.scroll);
   }

   public EditorPanel() {
      this(squareNoTopInsets);
   }

   protected void add(EditorPair pair) {
      LocalizableLabel label = pair.getLabel();
      ExtendedPanel field = pair.getPanel();
      ExtendedPanel panel;
      GridBagConstraints c;
      if (this.paneNum == this.panels.size()) {
         panel = new ExtendedPanel(new GridBagLayout());
         panel.getInsets().set(0, 0, 0, 0);
         c = new GridBagConstraints();
         c.fill = 2;
         this.container.add(panel, this.del(0));
         this.panels.add(panel);
         this.constraints.add(c);
      } else {
         panel = (ExtendedPanel)this.panels.get(this.paneNum);
         c = (GridBagConstraints)this.constraints.get(this.paneNum);
      }

      c.anchor = 17;
      c.gridy = this.rowNum;
      c.gridx = 0;
      c.weightx = 0.1D;
      panel.add(label, c);
      c.anchor = 13;
      byte var10003 = this.rowNum;
      this.rowNum = (byte)(var10003 + 1);
      c.gridy = var10003;
      c.gridx = 1;
      c.weightx = 1.0D;
      panel.add(field, c);
      Collections.addAll(this.handlers, pair.getHandlers());
   }

   protected void nextPane() {
      this.rowNum = 0;
      ++this.paneNum;
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
}
