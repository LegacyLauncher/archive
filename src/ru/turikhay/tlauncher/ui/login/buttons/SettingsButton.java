package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.ImageButton;

public class SettingsButton extends ImageButton implements Blockable {
   private static final long serialVersionUID = 1321382157134544911L;
   private final LoginForm lf;
   private final JPopupMenu popup;
   private final LocalizableMenuItem accountManager;
   private final LocalizableMenuItem versionManager;
   private final LocalizableMenuItem settings;

   SettingsButton(LoginForm loginform) {
      this.lf = loginform;
      this.image = loadImage("settings.png");
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.popup = new JPopupMenu();
      this.settings = new LocalizableMenuItem("loginform.button.settings.launcher");
      this.settings.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsButton.this.lf.scene.setSidePanel(DefaultScene.SidePanel.SETTINGS);
         }
      });
      this.popup.add(this.settings);
      this.versionManager = new LocalizableMenuItem("loginform.button.settings.version");
      this.versionManager.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsButton.this.lf.pane.openVersionManager();
         }
      });
      this.popup.add(this.versionManager);
      this.accountManager = new LocalizableMenuItem("loginform.button.settings.account");
      this.accountManager.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsButton.this.lf.pane.openAccountEditor();
         }
      });
      this.popup.add(this.accountManager);
      this.setPreferredSize(new Dimension(30, this.getHeight()));
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsButton.this.callPopup();
         }
      });
      this.initImage();
   }

   void callPopup() {
      this.lf.defocus();
      this.popup.show(this, 0, this.getHeight());
   }

   public void block(Object reason) {
      if (reason.equals("auth") || reason.equals("launch")) {
         Blocker.blockComponents(reason, this.accountManager, this.versionManager);
      }

   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(reason, this.accountManager, this.versionManager);
   }
}
