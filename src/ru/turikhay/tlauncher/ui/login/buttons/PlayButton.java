package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPopupMenu;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;

public class PlayButton extends LocalizableButton implements Blockable, LoginForm.LoginStateListener {
   private PlayButton.PlayButtonState state;
   private final LoginForm loginForm;
   private int mouseX;
   private int mouseY;
   private final JPopupMenu wrongButtonMenu = new JPopupMenu();

   PlayButton(LoginForm lf) {
      LocalizableMenuItem wrongButtonItem = new LocalizableMenuItem("loginform.wrongbutton");
      wrongButtonItem.setEnabled(false);
      wrongButtonItem.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            PlayButton.this.wrongButtonMenu.setVisible(false);
         }
      });
      this.wrongButtonMenu.add(wrongButtonItem);
      this.loginForm = lf;
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            switch(PlayButton.this.state) {
            case CANCEL:
               PlayButton.this.loginForm.stopLauncher();
               break;
            default:
               PlayButton.this.loginForm.startLauncher();
            }

         }
      });
      this.addMouseMotionListener(new MouseMotionAdapter() {
         public void mouseMoved(MouseEvent e) {
            PlayButton.this.mouseX = e.getX();
            PlayButton.this.mouseY = e.getY();
         }
      });
      this.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            if (e.getButton() != 1) {
               PlayButton.this.wrongButtonMenu.show(PlayButton.this, PlayButton.this.mouseX, PlayButton.this.mouseY);
            }

         }
      });
      this.setFont(this.getFont().deriveFont(1).deriveFont(TLauncherFrame.getFontSize() * 1.5F));
      this.setState(PlayButton.PlayButtonState.PLAY);
   }

   public void setState(PlayButton.PlayButtonState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         this.setText(state.getPath());
         if (state == PlayButton.PlayButtonState.CANCEL) {
            this.setEnabled(true);
         }

      }
   }

   public void updateState() {
      VersionSyncInfo vs = this.loginForm.versions.getVersion();
      if (vs != null) {
         boolean installed = vs.isInstalled();
         boolean force = this.loginForm.checkbox.forceupdate.getState();
         if (!installed) {
            this.setState(PlayButton.PlayButtonState.INSTALL);
         } else {
            this.setState(force ? PlayButton.PlayButtonState.REINSTALL : PlayButton.PlayButtonState.PLAY);
         }
      }

   }

   public void loginStateChanged(LoginForm.LoginState state) {
      if (state == LoginForm.LoginState.LAUNCHING) {
         this.setState(PlayButton.PlayButtonState.CANCEL);
      } else {
         this.updateState();
         this.setEnabled(!Blocker.isBlocked(this));
      }

   }

   public void block(Object reason) {
      if (this.state != PlayButton.PlayButtonState.CANCEL) {
         this.setEnabled(false);
      }

   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }

   public static enum PlayButtonState {
      REINSTALL("loginform.enter.reinstall"),
      INSTALL("loginform.enter.install"),
      PLAY("loginform.enter"),
      CANCEL("loginform.enter.cancel");

      private final String path;

      private PlayButtonState(String path) {
         this.path = path;
      }

      public String getPath() {
         return this.path;
      }
   }
}
