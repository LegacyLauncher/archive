package ru.turikhay.tlauncher.ui.console;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class ConsoleFrameBottom extends BorderPanel implements LocalizableComponent {
   private final ConsoleFrame frame;
   public final LocalizableButton closeCancelButton;
   public final ExtendedButton save;
   public final ExtendedButton pastebin;
   public final ExtendedButton kill;

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
      this.save = this.newButton("document-save-as.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ConsoleFrameBottom.this.frame.console.saveAs();
         }
      });
      this.pastebin = this.newButton("mail-attachment.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ConsoleFrameBottom.this.frame.console.sendPaste();
         }
      });
      this.kill = this.newButton("process-stop.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ConsoleFrameBottom.this.frame.console.launcher.killProcess();
            ConsoleFrameBottom.this.kill.setEnabled(false);
         }
      });
      this.kill.setEnabled(false);
      this.updateLocale();
      ExtendedPanel buttonPanel = new ExtendedPanel();
      buttonPanel.add(this.save, this.pastebin, this.kill);
      this.setEast(buttonPanel);
   }

   private ExtendedButton newButton(String path, ActionListener action) {
      ExtendedButton button = new ExtendedButton();
      button.addActionListener(action);
      button.setIcon(Images.getIcon(path));
      button.setPreferredSize(new Dimension(32, 32));
      return button;
   }

   public void updateLocale() {
      this.save.setToolTipText(Localizable.get("console.save"));
      this.pastebin.setToolTipText(Localizable.get("console.pastebin"));
      this.kill.setToolTipText(Localizable.get("console.kill"));
   }
}
