package com.turikhay.tlauncher.ui.scenes;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.animate.Animator;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.settings.SettingsPanel;

public class DefaultScene extends PseudoScene {
   private static final long serialVersionUID = -1460877989848190921L;
   final int LOGINFORM_WIDTH;
   final int LOGINFORM_HEIGHT;
   final int SETTINGSFORM_WIDTH;
   final int SETTINGSFORM_HEIGHT;
   final int MARGIN;
   public final LoginForm loginForm;
   public final SettingsPanel settingsForm;
   private boolean settings;

   public DefaultScene(MainPane main) {
      super(main);
      this.LOGINFORM_WIDTH = this.LOGINFORM_HEIGHT = 240;
      this.SETTINGSFORM_WIDTH = 500;
      this.SETTINGSFORM_HEIGHT = 475;
      this.MARGIN = 25;
      this.settingsForm = new SettingsPanel(this);
      this.settingsForm.setSize(this.SETTINGSFORM_WIDTH, this.SETTINGSFORM_HEIGHT);
      this.add(this.settingsForm);
      this.loginForm = new LoginForm(this);
      this.loginForm.setSize(this.LOGINFORM_WIDTH, this.LOGINFORM_HEIGHT);
      this.add(this.loginForm);
      this.setSettings(false, false);
   }

   public void onResize() {
      super.onResize();
      this.setSettings(this.settings, false);
   }

   public void setSettings(boolean shown, boolean update) {
      if (this.settings != shown || !update) {
         if (shown) {
            this.settingsForm.unblock("");
         } else {
            this.settingsForm.block("");
         }

         if (update) {
            this.settingsForm.saveValues();
         }

         int w = this.getWidth();
         int h = this.getHeight();
         int hw = w / 2;
         int hh = h / 2;
         int lf_x;
         int lf_y;
         int sf_x;
         int sf_y;
         if (shown) {
            int bw = this.LOGINFORM_WIDTH + this.SETTINGSFORM_WIDTH + this.MARGIN;
            int hbw = bw / 2;
            lf_x = hw - hbw;
            lf_y = hh - this.LOGINFORM_HEIGHT / 2;
            sf_x = hw - hbw + this.SETTINGSFORM_WIDTH / 2 + this.MARGIN;
            sf_y = hh - this.SETTINGSFORM_HEIGHT / 2;
         } else {
            lf_x = hw - this.LOGINFORM_WIDTH / 2;
            lf_y = hh - this.LOGINFORM_HEIGHT / 2;
            sf_x = w * 2;
            sf_y = hh - this.SETTINGSFORM_HEIGHT / 2;
         }

         Animator.move(this.loginForm, lf_x, lf_y);
         Animator.move(this.settingsForm, sf_x, sf_y);
         this.settings = shown;
      }
   }

   public void setSettings(boolean shown) {
      this.setSettings(shown, true);
   }

   public void toggleSettings() {
      this.setSettings(!this.settings);
   }

   public void block(Object reason) {
      Blocker.block((Blockable)this.loginForm, (Object)reason);
   }

   public void unblock(Object reason) {
      Blocker.unblock((Blockable)this.loginForm, (Object)reason);
   }
}
