package ru.turikhay.tlauncher.ui.frames;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Box;
import javax.swing.JFrame;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class ActionFrame extends JFrame implements LocalizableComponent {
   private String title;
   private final LocalizableLabel head;
   private final BorderPanel holder;
   private final VPanel body;
   private final LocalizableHTMLLabel bodyText;
   private final ExtendedPanel footer;

   public ActionFrame(Dimension minSize) {
      this.setIconImages(SwingUtil.getFavicons());
      this.setMinimumSize(SwingUtil.magnify(minSize));
      this.setSize(SwingUtil.magnify(minSize));
      this.holder = new BorderPanel();
      this.holder.setVgap(5);
      this.holder.setInsets(new MagnifiedInsets(10, 20, 20, 20));
      this.add(this.holder);
      this.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            ActionFrame.this.holder.setPreferredSize(ActionFrame.this.getRootPane().getSize());
            ActionFrame.this.bodyText.setMinimumSize(new Dimension(Integer.MAX_VALUE, 1));
         }
      });
      this.head = new LocalizableLabel();
      this.head.setFont(this.head.getFont().deriveFont(this.head.getFont().getSize2D() + 18.0F).deriveFont(1));
      this.head.setForeground(new Color(this.head.getForeground().getRed(), this.head.getForeground().getGreen(), this.head.getForeground().getBlue(), 128));
      this.holder.setNorth(this.head);
      this.body = new VPanel();
      this.body.add(Box.createRigidArea(SwingUtil.magnify(new Dimension(1, 4))));
      this.holder.setCenter(this.body);
      this.bodyText = new LocalizableHTMLLabel();
      this.body.add(this.bodyText);
      this.footer = new ExtendedPanel();
      this.holder.setSouth(this.footer);
      this.updateLocale();
   }

   public void showAndWait() {
      this.showAtCenter();

      while(this.isDisplayable()) {
         U.sleepFor(100L);
      }

   }

   public void showAtCenter() {
      this.setVisible(true);
      this.setLocationRelativeTo((Component)null);
   }

   public final void setTitlePath(String title) {
      this.title = title;
      this.updateTitle();
   }

   public final LocalizableLabel getHead() {
      return this.head;
   }

   public final VPanel getBody() {
      return this.body;
   }

   public final LocalizableHTMLLabel getBodyText() {
      return this.bodyText;
   }

   public final ExtendedPanel getFooter() {
      return this.footer;
   }

   public void updateLocale() {
      this.updateTitle();
      Localizable.updateContainer(this);
   }

   private void updateTitle() {
      this.setTitle(Localizable.get(this.title));
   }
}
