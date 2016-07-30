package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;
import javax.swing.JLabel;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.ImageIcon;

public class ExtendedLabel extends JLabel {
   private static final AlphaComposite disabledAlphaComposite = AlphaComposite.getInstance(3, 0.5F);

   public ExtendedLabel(String text, Icon icon, int horizontalAlignment) {
      super(text, icon, horizontalAlignment);
      this.setFont(this.getFont().deriveFont(TLauncherFrame.getFontSize()));
      this.setOpaque(false);
   }

   public ExtendedLabel(String text, int horizontalAlignment) {
      this(text, (Icon)null, horizontalAlignment);
   }

   public ExtendedLabel() {
      this((String)null, (Icon)null, 10);
   }

   public void setIcon(ImageIcon icon) {
      super.setIcon(icon);
      super.setDisabledIcon(icon == null ? null : icon.getDisabledInstance());
   }

   public void paintComponent(Graphics g0) {
      if (this.isEnabled()) {
         super.paintComponent(g0);
      } else {
         Graphics2D g = (Graphics2D)g0;
         Composite oldComposite = g.getComposite();
         g.setComposite(disabledAlphaComposite);
         super.paintComponent(g);
         g.setComposite(oldComposite);
      }

   }
}
