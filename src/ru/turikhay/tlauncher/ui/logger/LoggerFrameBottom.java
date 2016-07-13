package ru.turikhay.tlauncher.ui.logger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

public class LoggerFrameBottom extends BorderPanel implements LocalizableComponent {
   private final LoggerFrame frame;
   public final LocalizableButton closeCancelButton;
   public final ExtendedButton folder;
   public final ExtendedButton save;
   public final ExtendedButton pastebin;
   public final ExtendedButton kill;
   File openFolder;

   LoggerFrameBottom(LoggerFrame fr) {
      this.frame = fr;
      this.setOpaque(true);
      this.setBackground(Color.darkGray);
      this.closeCancelButton = new LocalizableButton("logger.close.cancel");
      this.closeCancelButton.setVisible(false);
      this.closeCancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (LoggerFrameBottom.this.closeCancelButton.isVisible()) {
               LoggerFrameBottom.this.frame.hiding = false;
               LoggerFrameBottom.this.closeCancelButton.setVisible(false);
            }

         }
      });
      this.setCenter(this.closeCancelButton);
      this.folder = this.newButton("folder.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            OS.openFolder(LoggerFrameBottom.this.openFolder == null ? MinecraftUtil.getWorkingDirectory() : LoggerFrameBottom.this.openFolder);
         }
      });
      this.save = this.newButton("document-save-as.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            LoggerFrameBottom.this.frame.logger.saveAs();
         }
      });
      this.pastebin = this.newButton("mail-attachment.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            LoggerFrameBottom.this.frame.logger.sendPaste();
         }
      });
      this.kill = this.newButton("process-stop.png", new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            LoggerFrameBottom.this.frame.logger.launcher.killProcess();
            LoggerFrameBottom.this.kill.setEnabled(false);
         }
      });
      this.kill.setEnabled(false);
      this.updateLocale();
      ExtendedPanel buttonPanel = new ExtendedPanel();
      buttonPanel.add(this.folder, this.save, this.pastebin, this.kill);
      this.setEast(buttonPanel);
   }

   private ExtendedButton newButton(String path, ActionListener action) {
      ExtendedButton button = new ExtendedButton();
      button.addActionListener(action);
      button.setIcon(Images.getIcon(path, SwingUtil.magnify(22), SwingUtil.magnify(22)));
      button.setPreferredSize(new Dimension(SwingUtil.magnify(32), SwingUtil.magnify(32)));
      return button;
   }

   public void updateLocale() {
      this.save.setToolTipText(Localizable.get("logger.save"));
      this.pastebin.setToolTipText(Localizable.get("logger.pastebin"));
      this.kill.setToolTipText(Localizable.get("logger.kill"));
   }
}
