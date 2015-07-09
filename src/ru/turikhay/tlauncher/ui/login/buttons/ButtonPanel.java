package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import ru.turikhay.tlauncher.ui.block.BlockablePanel;
import ru.turikhay.tlauncher.ui.login.LoginForm;

public class ButtonPanel extends BlockablePanel {
   private static final long serialVersionUID = -2155145867054136409L;
   public final PlayButton play;
   private final JPanel manageButtonsPanel;
   public final SupportButton support;
   public final FolderButton folder;
   public final RefreshButton refresh;
   public final SettingsButton settings;
   public final CancelAutoLoginButton cancel;
   private ButtonPanel.ButtonPanelState state;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$ButtonPanel$ButtonPanelState;

   public ButtonPanel(LoginForm lf) {
      boolean letUserDoWhatHeWants = !lf.global.getBoolean("gui.settings.blocked");
      BorderLayout lm = new BorderLayout(1, 2);
      this.setLayout(lm);
      this.setOpaque(false);
      this.play = new PlayButton(lf);
      this.add("Center", this.play);
      this.cancel = new CancelAutoLoginButton(lf);
      this.manageButtonsPanel = new JPanel(new GridLayout(0, letUserDoWhatHeWants ? 4 : 2));
      this.manageButtonsPanel.setOpaque(false);
      this.support = new SupportButton(lf);
      this.manageButtonsPanel.add(this.support);
      this.folder = new FolderButton(lf);
      if (letUserDoWhatHeWants) {
         this.manageButtonsPanel.add(this.folder);
      }

      this.refresh = new RefreshButton(lf);
      this.manageButtonsPanel.add(this.refresh);
      this.settings = new SettingsButton(lf);
      if (letUserDoWhatHeWants) {
         this.manageButtonsPanel.add(this.settings);
      }

      this.setState(lf.autologin.isEnabled() ? ButtonPanel.ButtonPanelState.AUTOLOGIN_CANCEL : ButtonPanel.ButtonPanelState.MANAGE_BUTTONS);
   }

   public ButtonPanel.ButtonPanelState getState() {
      return this.state;
   }

   public void setState(ButtonPanel.ButtonPanelState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$ButtonPanel$ButtonPanelState()[state.ordinal()]) {
         case 1:
            this.remove(this.manageButtonsPanel);
            this.add("South", this.cancel);
            break;
         case 2:
            this.remove(this.cancel);
            this.add("South", this.manageButtonsPanel);
            break;
         default:
            throw new IllegalArgumentException("Unknown state: " + state);
         }

         this.validate();
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$ButtonPanel$ButtonPanelState() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$ButtonPanel$ButtonPanelState;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ButtonPanel.ButtonPanelState.values().length];

         try {
            var0[ButtonPanel.ButtonPanelState.AUTOLOGIN_CANCEL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ButtonPanel.ButtonPanelState.MANAGE_BUTTONS.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$ui$login$buttons$ButtonPanel$ButtonPanelState = var0;
         return var0;
      }
   }

   public static enum ButtonPanelState {
      AUTOLOGIN_CANCEL,
      MANAGE_BUTTONS;
   }
}
