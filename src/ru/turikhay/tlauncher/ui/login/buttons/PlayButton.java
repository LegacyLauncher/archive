package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;

public class PlayButton extends LocalizableButton implements Blockable, LoginForm.LoginStateListener {
   private static final long serialVersionUID = 6944074583143406549L;
   private PlayButton.PlayButtonState state;
   private final LoginForm loginForm;

   PlayButton(LoginForm lf) {
      this.loginForm = lf;
      this.addActionListener(new ActionListener() {
         // $FF: synthetic field
         private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$PlayButton$PlayButtonState;

         public void actionPerformed(ActionEvent e) {
            switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$PlayButton$PlayButtonState()[PlayButton.this.state.ordinal()]) {
            case 4:
               PlayButton.this.loginForm.stopLauncher();
               break;
            default:
               PlayButton.this.loginForm.startLauncher();
            }

         }

         // $FF: synthetic method
         static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$PlayButton$PlayButtonState() {
            int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$PlayButton$PlayButtonState;
            if (var10000 != null) {
               return var10000;
            } else {
               int[] var0 = new int[PlayButton.PlayButtonState.values().length];

               try {
                  var0[PlayButton.PlayButtonState.CANCEL.ordinal()] = 4;
               } catch (NoSuchFieldError var4) {
               }

               try {
                  var0[PlayButton.PlayButtonState.INSTALL.ordinal()] = 2;
               } catch (NoSuchFieldError var3) {
               }

               try {
                  var0[PlayButton.PlayButtonState.PLAY.ordinal()] = 3;
               } catch (NoSuchFieldError var2) {
               }

               try {
                  var0[PlayButton.PlayButtonState.REINSTALL.ordinal()] = 1;
               } catch (NoSuchFieldError var1) {
               }

               $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$PlayButton$PlayButtonState = var0;
               return var0;
            }
         }
      });
      this.setFont(this.getFont().deriveFont(1).deriveFont(16.0F));
      this.setState(PlayButton.PlayButtonState.PLAY);
   }

   public PlayButton.PlayButtonState getState() {
      return this.state;
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
