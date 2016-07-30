package ru.turikhay.tlauncher.ui.frames;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Box;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;

public class VActionFrame extends ActionFrame {
   private final LocalizableLabel head;
   private final BorderPanel holder;
   private final VPanel body;
   private final LocalizableHTMLLabel bodyText;
   private final ExtendedPanel footer;

   public VActionFrame(int width) {
      this.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
      this.setIconImages(SwingUtil.getFavicons());
      this.holder = new BorderPanel();
      this.holder.setVgap(5);
      this.holder.setInsets(new MagnifiedInsets(10, 20, 20, 20));
      this.add(this.holder);
      this.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            VActionFrame.this.holder.setPreferredSize(VActionFrame.this.getRootPane().getSize());
            VActionFrame.this.bodyText.setMinimumSize(new Dimension(Integer.MAX_VALUE, 1));
         }
      });
      this.head = new LocalizableLabel();
      this.head.setFont(this.head.getFont().deriveFont(this.head.getFont().getSize2D() + 18.0F).deriveFont(1));
      this.head.setForeground(new Color(this.head.getForeground().getRed(), this.head.getForeground().getGreen(), this.head.getForeground().getBlue(), 128));
      this.head.setIconTextGap(SwingUtil.magnify(10));
      this.holder.setNorth(this.head);
      this.body = new VPanel();
      this.body.add(Box.createRigidArea(SwingUtil.magnify(new Dimension(1, 4))));
      this.holder.setCenter(this.body);
      this.bodyText = new LocalizableHTMLLabel();
      this.bodyText.setLabelWidth(width);
      this.body.add(this.bodyText);
      this.footer = new ExtendedPanel();
      this.holder.setSouth(this.footer);
   }

   public VActionFrame() {
      this(500);
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
}
