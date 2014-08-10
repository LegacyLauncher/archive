package ru.turikhay.tlauncher.ui.scenes;

import java.awt.Component;
import ru.turikhay.tlauncher.ui.InfoPanel;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.animate.Animator;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.util.Direction;

public class DefaultScene extends PseudoScene {
   private static final long serialVersionUID = -1460877989848190921L;
   private final int LOGINFORM_WIDTH = 250;
   private final int LOGINFORM_HEIGHT = 240;
   private final int SETTINGSFORM_WIDTH = 500;
   private final int SETTINGSFORM_HEIGHT = 475;
   private final int MARGIN = 25;
   public final LoginForm loginForm;
   public final SettingsPanel settingsForm = new SettingsPanel(this);
   private final InfoPanel infoPanel;
   private Direction direction;
   private boolean settings;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$util$Direction;

   public DefaultScene(MainPane main) {
      super(main);
      this.settingsForm.setSize(this.SETTINGSFORM_WIDTH, this.SETTINGSFORM_HEIGHT);
      this.add(this.settingsForm);
      this.loginForm = new LoginForm(this);
      this.loginForm.setSize(this.LOGINFORM_WIDTH, this.LOGINFORM_HEIGHT);
      this.add(this.loginForm);
      this.infoPanel = new InfoPanel(this);
      this.infoPanel.setSize(200, 35);
      this.add(this.infoPanel);
      this.loadDirection();
   }

   public Direction getDirection() {
      return this.direction;
   }

   public void onResize() {
      if (this.parent != null) {
         this.setBounds(0, 0, this.parent.getWidth(), this.parent.getHeight());
         this.setSettings(this.settings, false);
      }
   }

   void setSettings(boolean shown, boolean update) {
      if (this.settings != shown || !update) {
         if (shown) {
            this.settingsForm.unblock("");
         } else {
            this.settingsForm.block("");
         }

         int w = this.getWidth();
         int h = this.getHeight();
         int hw = w / 2;
         int hh = h / 2;
         int lf_x;
         int lf_y;
         int sf_x;
         int sf_y;
         int hbw;
         if (shown) {
            int bw = this.LOGINFORM_WIDTH + this.SETTINGSFORM_WIDTH + this.MARGIN;
            hbw = bw / 2;
            lf_y = hh - this.LOGINFORM_HEIGHT / 2;
            sf_y = hh - this.SETTINGSFORM_HEIGHT / 2;
            if (bw > w) {
               lf_x = -this.LOGINFORM_WIDTH;
               sf_x = hw - this.SETTINGSFORM_WIDTH / 2;
            } else {
               lf_x = hw - hbw;
               sf_x = hw - hbw + this.SETTINGSFORM_WIDTH / 2 + this.MARGIN;
               sf_y = hh - this.SETTINGSFORM_HEIGHT / 2;
            }
         } else {
            switch($SWITCH_TABLE$ru$turikhay$util$Direction()[this.direction.ordinal()]) {
            case 1:
               lf_x = this.MARGIN;
               lf_y = this.MARGIN;
               break;
            case 2:
               lf_x = hw - this.LOGINFORM_WIDTH / 2;
               lf_y = this.MARGIN;
               break;
            case 3:
               lf_x = w - this.LOGINFORM_WIDTH - this.MARGIN;
               lf_y = this.MARGIN;
               break;
            case 4:
               lf_x = this.MARGIN;
               lf_y = hh - this.LOGINFORM_HEIGHT / 2;
               break;
            case 5:
               lf_x = hw - this.LOGINFORM_WIDTH / 2;
               lf_y = hh - this.LOGINFORM_HEIGHT / 2;
               break;
            case 6:
               lf_x = w - this.LOGINFORM_WIDTH - this.MARGIN;
               lf_y = hh - this.LOGINFORM_HEIGHT / 2;
               break;
            case 7:
               lf_x = this.MARGIN;
               lf_y = h - this.LOGINFORM_HEIGHT - this.MARGIN;
               break;
            case 8:
               lf_x = hw - this.LOGINFORM_WIDTH / 2;
               lf_y = h - this.LOGINFORM_HEIGHT - this.MARGIN;
               break;
            case 9:
               lf_x = w - this.LOGINFORM_WIDTH - this.MARGIN;
               lf_y = h - this.LOGINFORM_HEIGHT - this.MARGIN;
               break;
            default:
               throw new IllegalArgumentException();
            }

            sf_x = w * 2;
            sf_y = hh - this.SETTINGSFORM_HEIGHT / 2;
         }

         Animator.move(this.loginForm, lf_x, lf_y);
         Animator.move(this.settingsForm, sf_x, sf_y);
         this.infoPanel.setShown(!shown, false);
         Component[] var14;
         int var13 = (var14 = this.getComponents()).length;

         for(hbw = 0; hbw < var13; ++hbw) {
            Component comp = var14[hbw];
            if (comp instanceof ResizeableComponent) {
               ((ResizeableComponent)comp).onResize();
            }
         }

         this.settings = shown;
      }
   }

   public void setSettings(boolean shown) {
      this.setSettings(shown, true);
   }

   public void toggleSettings() {
      this.setSettings(!this.settings);
   }

   public boolean isSettingsShown() {
      return this.settings;
   }

   private void loadDirection() {
      Direction dir = this.getMainPane().getRootFrame().getConfiguration().getDirection("gui.direction.lf");
      if (dir == null) {
         dir = Direction.CENTER;
      }

      this.direction = dir;
   }

   public void updateDirection() {
      this.loadDirection();
      if (!this.settings) {
         this.setSettings(true, false);
      }

   }

   public void block(Object reason) {
      Blocker.block(reason, this.loginForm, this.settingsForm);
   }

   public void unblock(Object reason) {
      Blocker.unblock(reason, this.loginForm, this.settingsForm);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$util$Direction() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$util$Direction;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Direction.values().length];

         try {
            var0[Direction.BOTTOM.ordinal()] = 8;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[Direction.BOTTOM_LEFT.ordinal()] = 7;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[Direction.BOTTOM_RIGHT.ordinal()] = 9;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[Direction.CENTER.ordinal()] = 5;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[Direction.CENTER_LEFT.ordinal()] = 4;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[Direction.CENTER_RIGHT.ordinal()] = 6;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[Direction.TOP.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Direction.TOP_LEFT.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Direction.TOP_RIGHT.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$util$Direction = var0;
         return var0;
      }
   }
}
