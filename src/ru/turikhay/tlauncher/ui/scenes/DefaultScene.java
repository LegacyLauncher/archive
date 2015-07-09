package ru.turikhay.tlauncher.ui.scenes;

import java.awt.Dimension;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.NoticePanel;
import ru.turikhay.tlauncher.ui.SideNotifier;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.Direction;

public class DefaultScene extends PseudoScene {
   public static final Dimension LOGIN_SIZE = new Dimension(250, 240);
   public static final Dimension SETTINGS_SIZE = new Dimension(500, 500);
   public static final int EDGE_INSETS = 10;
   public static final int INSETS = 15;
   public final SideNotifier notifier;
   public final LoginForm loginForm;
   public final SettingsPanel settingsForm;
   private DefaultScene.SidePanel sidePanel;
   private ExtendedPanel sidePanelComp;
   private Direction lfDirection;
   public final NoticePanel infoPanel;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$util$Direction;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$scenes$DefaultScene$SidePanel;

   public DefaultScene(MainPane main) {
      super(main);
      this.notifier = main.notifier;
      this.settingsForm = new SettingsPanel(this);
      this.settingsForm.setSize(SETTINGS_SIZE);
      this.settingsForm.setVisible(false);
      this.add(this.settingsForm);
      this.loginForm = new LoginForm(this);
      this.loginForm.setSize(LOGIN_SIZE);
      this.add(this.loginForm);
      this.infoPanel = new NoticePanel(this);
      this.add(this.infoPanel);
      this.updateDirection();
   }

   public void onResize() {
      if (this.parent != null) {
         this.setBounds(0, 0, this.parent.getWidth(), this.parent.getHeight());
         this.updateCoords();
      }
   }

   private void updateCoords() {
      int w = this.getWidth();
      int h = this.getHeight();
      int hw = w / 2;
      int hh = h / 2;
      int lf_w = this.loginForm.getWidth();
      int lf_h = this.loginForm.getHeight();
      int lf_x;
      int lf_y;
      int n_x;
      if (this.sidePanel == null) {
         switch($SWITCH_TABLE$ru$turikhay$util$Direction()[this.lfDirection.ordinal()]) {
         case 1:
         case 4:
         case 7:
            lf_x = 10;
            break;
         case 2:
         case 5:
         case 8:
            lf_x = hw - lf_w / 2;
            break;
         case 3:
         case 6:
         case 9:
            lf_x = w - lf_w - 10;
            break;
         default:
            throw new RuntimeException("unknown direction:" + this.lfDirection);
         }

         switch($SWITCH_TABLE$ru$turikhay$util$Direction()[this.lfDirection.ordinal()]) {
         case 1:
         case 2:
         case 3:
            lf_y = 10;
            break;
         case 4:
         case 5:
         case 6:
            lf_y = hh - lf_h / 2;
            break;
         case 7:
         case 8:
         case 9:
            lf_y = h - 10 - lf_h;
            break;
         default:
            throw new RuntimeException("unknown direction:" + this.lfDirection);
         }
      } else {
         n_x = this.sidePanelComp.getWidth();
         int sp_h = this.sidePanelComp.getHeight();
         int bw = lf_w + n_x + 15;
         int hbw = bw / 2;
         int sp_x;
         int sp_y;
         if (w > bw) {
            switch($SWITCH_TABLE$ru$turikhay$util$Direction()[this.lfDirection.ordinal()]) {
            case 1:
            case 4:
            case 7:
               lf_x = 10;
               sp_x = lf_x + lf_w + 15;
               break;
            case 2:
            case 5:
            case 8:
               lf_x = hw - hbw;
               sp_x = lf_x + 15 + n_x / 2;
               break;
            case 3:
            case 6:
            case 9:
               lf_x = w - 10 - lf_w;
               sp_x = lf_x - 15 - n_x;
               break;
            default:
               throw new RuntimeException("unknown direction:" + this.lfDirection);
            }

            switch($SWITCH_TABLE$ru$turikhay$util$Direction()[this.lfDirection.ordinal()]) {
            case 1:
            case 2:
            case 3:
               sp_y = 10;
               lf_y = 10;
               break;
            case 4:
            case 5:
            case 6:
               lf_y = hh - lf_h / 2;
               sp_y = hh - sp_h / 2;
               break;
            case 7:
            case 8:
            case 9:
               lf_y = h - 10 - lf_h;
               sp_y = h - 10 - sp_h;
               break;
            default:
               throw new RuntimeException("unknown direction:" + this.lfDirection);
            }
         } else {
            lf_x = w * 2;
            lf_y = 0;
            sp_x = hw - n_x / 2;
            sp_y = hh - sp_h / 2;
         }

         this.sidePanelComp.setLocation(sp_x, sp_y);
      }

      int n_y = 10;
      switch($SWITCH_TABLE$ru$turikhay$util$Direction()[this.lfDirection.ordinal()]) {
      case 1:
      case 4:
      case 7:
         n_x = this.getMainPane().getWidth() - 10 - this.notifier.getWidth();
         break;
      case 2:
      case 3:
      case 5:
      case 6:
      default:
         n_x = 10;
      }

      this.notifier.setLocation(n_x, n_y);
      this.loginForm.setLocation(lf_x, lf_y);
      this.infoPanel.onResize();
   }

   public DefaultScene.SidePanel getSidePanel() {
      return this.sidePanel;
   }

   public void setSidePanel(DefaultScene.SidePanel side) {
      if (this.sidePanel != side) {
         boolean noSidePanel = side == null;
         if (this.sidePanelComp != null) {
            this.sidePanelComp.setVisible(false);
         }

         this.sidePanel = side;
         this.sidePanelComp = noSidePanel ? null : this.getSidePanelComp(side);
         if (!noSidePanel) {
            this.sidePanelComp.setVisible(true);
         }

         this.infoPanel.setShown(noSidePanel, noSidePanel);
         this.updateCoords();
      }
   }

   public void toggleSidePanel(DefaultScene.SidePanel side) {
      if (this.sidePanel == side) {
         side = null;
      }

      this.setSidePanel(side);
   }

   public ExtendedPanel getSidePanelComp(DefaultScene.SidePanel side) {
      if (side == null) {
         throw new NullPointerException("side");
      } else {
         switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$scenes$DefaultScene$SidePanel()[side.ordinal()]) {
         case 1:
            return this.settingsForm;
         default:
            throw new RuntimeException("unknown side:" + side);
         }
      }
   }

   public Direction getLoginFormDirection() {
      return this.lfDirection;
   }

   public void updateDirection() {
      this.loadDirection();
      this.updateCoords();
   }

   private void loadDirection() {
      Configuration config = this.getMainPane().getRootFrame().getConfiguration();
      Direction loginFormDirection = config.getDirection("gui.direction.loginform");
      if (loginFormDirection == null) {
         loginFormDirection = Direction.CENTER;
      }

      this.lfDirection = loginFormDirection;
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

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$scenes$DefaultScene$SidePanel() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$scenes$DefaultScene$SidePanel;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[DefaultScene.SidePanel.values().length];

         try {
            var0[DefaultScene.SidePanel.SETTINGS.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$ui$scenes$DefaultScene$SidePanel = var0;
         return var0;
      }
   }

   public static enum SidePanel {
      SETTINGS;

      public final boolean requiresShow;

      private SidePanel(boolean requiresShow) {
         this.requiresShow = requiresShow;
      }

      private SidePanel() {
         this(false);
      }
   }
}
