package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import ru.turikhay.tlauncher.ui.block.BlockablePanel;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.SwingUtil;

public class ButtonPanel extends BlockablePanel {
   public final PlayButton play;
   private final JPanel manageButtonsPanel;
   public final SupportButton support;
   public final FolderButton folder;
   public final RefreshButton refresh;
   public final SettingsButton settings;
   public final CancelAutoLoginButton cancel;
   private ButtonPanel.ButtonPanelState state;

   public ButtonPanel(LoginForm lf) {
      boolean letUserDoWhatHeWants = !lf.global.getBoolean("gui.settings.blocked");
      BorderLayout lm = new BorderLayout(SwingUtil.magnify(1), SwingUtil.magnify(2));
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

   public void setState(ButtonPanel.ButtonPanelState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         switch(state) {
         case AUTOLOGIN_CANCEL:
            this.remove(this.manageButtonsPanel);
            this.add("South", this.cancel);
            break;
         case MANAGE_BUTTONS:
            this.remove(this.cancel);
            this.add("South", this.manageButtonsPanel);
            break;
         default:
            throw new IllegalArgumentException("Unknown state: " + state);
         }

         this.validate();
      }
   }

   public static enum ButtonPanelState {
      AUTOLOGIN_CANCEL,
      MANAGE_BUTTONS;
   }
}
