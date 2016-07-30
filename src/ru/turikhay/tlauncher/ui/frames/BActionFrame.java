package ru.turikhay.tlauncher.ui.frames;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window.Type;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

public class BActionFrame extends ActionFrame {
   private final LocalizableLabel head;
   private final BorderPanel body;
   private final ExtendedPanel footer;

   public BActionFrame(Insets insets) {
      this.setDefaultCloseOperation(1);
      if (OS.JAVA_VERSION.getDouble() > 1.6D) {
         this.setType(Type.UTILITY);
      }

      this.head = new LocalizableLabel();
      this.body = new BorderPanel();
      this.body.setHgap(SwingUtil.magnify(5));
      this.body.setVgap(SwingUtil.magnify(5));
      this.footer = new ExtendedPanel();
      BorderPanel holder = new BorderPanel();
      holder.setHgap(SwingUtil.magnify(10));
      holder.setVgap(SwingUtil.magnify(10));
      holder.setNorth(this.head);
      holder.setCenter(this.body);
      holder.setSouth(this.footer);
      ExtendedPanel centerPanel = new ExtendedPanel();
      centerPanel.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.insets = SwingUtil.magnify(new Insets(15, 15, 15, 15));
      c.weightx = 1.0D;
      c.fill = 2;
      centerPanel.add(holder, c);
      this.add(centerPanel);
      this.pack();
   }

   public BActionFrame() {
      this((Insets)null);
   }

   public LocalizableLabel getHead() {
      return this.head;
   }

   public BorderPanel getBody() {
      return this.body;
   }
}
