package ru.turikhay.tlauncher.ui.console;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class ConsoleFrameBottom extends BorderPanel implements LocalizableComponent {
   private final ConsoleFrame frame;
   public final LocalizableButton closeCancelButton;
   public final ImageButton kill;

   ConsoleFrameBottom(ConsoleFrame fr) {
      this.frame = fr;
      this.setOpaque(true);
      this.setBackground(Color.darkGray);
      this.closeCancelButton = new LocalizableButton("console.close.cancel");
      this.closeCancelButton.setVisible(false);
      this.closeCancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (ConsoleFrameBottom.this.closeCancelButton.isVisible()) {
               ConsoleFrameBottom.this.frame.hiding = false;
               ConsoleFrameBottom.this.closeCancelButton.setVisible(false);
            }
         }
      });
      this.setCenter(this.closeCancelButton);
      this.kill = this.newButton("process-stop.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         }
      });
      this.kill.setEnabled(false);
      this.kill.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ConsoleFrameBottom.this.frame.console.launcher.killProcess();
            ConsoleFrameBottom.this.kill.setEnabled(false);
         }
      });
      this.updateLocale();
      ExtendedPanel buttonPanel = new ExtendedPanel();
      buttonPanel.add((Component)this.kill);
      this.setEast(buttonPanel);
   }

   private ImageButton newButton(String path, ActionListener action) {
      ImageButton button = new ImageButton(path);
      button.addActionListener(action);
      button.setPreferredSize(new Dimension(32, 32));
      return button;
   }

   public void updateLocale() {
      this.kill.setToolTipText(Localizable.get("console.kill"));
   }
}
