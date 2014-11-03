package ru.turikhay.tlauncher.ui;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.AnimatorAction;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.updater.AdParser;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.Direction;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public class InfoPanel extends CenterPanel implements ResizeableComponent, UpdaterListener, LocalizableComponent {
   private static final int MARGIN = 10;
   private static final float FONT_SIZE = 12.0F;
   private final InfoPanel.InfoPanelAnimator animator = new InfoPanel.InfoPanelAnimator();
   private final EditorPane browser;
   private final DefaultScene parent;
   private final Object animationLock = new Object();
   private final int timeFrame = 5;
   private float opacity;
   private boolean shown;
   private boolean canshow;
   private AdParser lastAd;
   private String content;
   private int width;
   private int height;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$util$Direction;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$updater$AdParser$AdType;

   public InfoPanel(DefaultScene parent) {
      super(CenterPanel.tipTheme, new Insets(5, 10, 5, 10));
      this.parent = parent;
      this.browser = new EditorPane(this.getFont().deriveFont(12.0F));
      this.browser.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
            if (!InfoPanel.this.onClick()) {
               e.consume();
            }

         }

         public void mousePressed(MouseEvent e) {
            if (!InfoPanel.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseReleased(MouseEvent e) {
            if (!InfoPanel.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseEntered(MouseEvent e) {
            if (!InfoPanel.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseExited(MouseEvent e) {
            if (!InfoPanel.this.isVisible()) {
               e.consume();
            }

         }
      });
      this.add(this.browser);
      this.shown = false;
      this.setVisible(false);
      TLauncher.getInstance().getUpdater().addListener(this);
   }

   void setContent(String text, int width, int height) {
      if (width >= 1 && height >= 1) {
         this.width = width;
         this.height = height;
         this.browser.setText(text);
         this.onResize();
      } else {
         throw new IllegalArgumentException();
      }
   }

   public void onResize() {
      Graphics g = this.getGraphics();
      if (g != null) {
         Insets insets = this.getInsets();
         int compWidth = this.width + insets.left + insets.right;
         int compHeight = this.height + insets.top + insets.bottom;
         Point loginFormLocation = this.parent.loginForm.getLocation();
         Dimension loginFormSize = this.parent.loginForm.getSize();
         int x = loginFormLocation.x + loginFormSize.width / 2 - compWidth / 2;
         if (x + compWidth > this.parent.getWidth() - 10) {
            x = this.parent.getWidth() - compWidth - 10;
         }

         if (x < 10) {
            x = 10;
         }

         int y;
         switch($SWITCH_TABLE$ru$turikhay$util$Direction()[this.parent.getLoginFormDirection().ordinal()]) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
            y = loginFormLocation.y + loginFormSize.height + 10;
            break;
         case 7:
         case 8:
         case 9:
            y = loginFormLocation.y - compHeight - 10;
            break;
         default:
            throw new IllegalArgumentException();
         }

         if (y + compHeight > this.parent.getHeight() - 10) {
            y = this.parent.getHeight() - compHeight - 10;
         }

         if (y < 10) {
            y = 10;
         }

         this.setBounds(x, y, compWidth, compHeight);
      }
   }

   public void paint(Graphics g0) {
      Graphics2D g = (Graphics2D)g0;
      Composite oldComp = g.getComposite();
      g.setComposite(AlphaComposite.getInstance(3, this.opacity));
      super.paint(g0);
      g.setComposite(oldComp);
   }

   public void show(boolean animate) {
      if (this.canshow) {
         this.onResize();
         if (!this.shown) {
            synchronized(this.animationLock) {
               this.setVisible(true);
               this.browser.setVisible(true);
               this.opacity = 0.0F;
               float selectedOpacity = 1.0F;
               if (animate) {
                  while(this.opacity < selectedOpacity) {
                     this.opacity += 0.01F;
                     if (this.opacity > selectedOpacity) {
                        this.opacity = selectedOpacity;
                     }

                     this.repaint();
                     U.sleepFor((long)this.timeFrame);
                  }
               } else {
                  this.opacity = selectedOpacity;
                  this.repaint();
               }

               this.shown = true;
            }
         }
      }
   }

   public void show() {
      this.animator.act(AnimatorAction.SHOW);
   }

   void hide(boolean animate) {
      if (this.shown) {
         synchronized(this.animationLock) {
            if (animate) {
               while(this.opacity > 0.0F) {
                  this.opacity -= 0.01F;
                  if (this.opacity < 0.0F) {
                     this.opacity = 0.0F;
                  }

                  this.repaint();
                  U.sleepFor((long)this.timeFrame);
               }
            }

            this.setVisible(false);
            this.browser.setVisible(false);
            if (!animate) {
               this.opacity = 0.0F;
            }

            this.shown = false;
         }
      }
   }

   public void hide() {
      this.animator.act(AnimatorAction.HIDE);
   }

   public void setShown(boolean shown, boolean animate) {
      if (animate) {
         if (shown) {
            this.show();
         } else {
            this.hide();
         }
      } else if (shown) {
         this.show(false);
      } else {
         this.hide(false);
      }

   }

   boolean onClick() {
      return this.shown;
   }

   public void onUpdaterRequesting(Updater u) {
      this.hide(true);
   }

   public void onUpdaterRequestError(Updater u) {
   }

   public void onUpdateFound(Update upd) {
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onAdFound(Updater u, AdParser ad) {
      this.lastAd = ad;
      this.updateAd();
   }

   public void updateLocale() {
      this.updateAd();
   }

   private void updateAd() {
      this.hide();
      this.canshow = this.prepareAd();
      if (this.parent.getSidePanel() != DefaultScene.SidePanel.SETTINGS) {
         this.show();
      }

   }

   private boolean prepareAd() {
      if (this.lastAd == null) {
         return false;
      } else {
         String locale = this.parent.getMainPane().getRootFrame().getLauncher().getSettings().getLocale().toString();
         AdParser.Ad ad = this.lastAd.get(locale);
         if (ad == null) {
            return false;
         } else {
            StringBuilder builder = new StringBuilder();
            builder.append("<table width=\"").append(ad.getWidth()).append("\" height=\"").append(ad.getHeight()).append("\"><tr><td align=\"center\" valign=\"center\">");
            switch($SWITCH_TABLE$ru$turikhay$tlauncher$updater$AdParser$AdType()[ad.getType().ordinal()]) {
            case 1:
               if (ad.getImage() != null) {
                  builder.append("<img src=\"").append(ad.getImage()).append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");
               }

               builder.append(ad.getContent());
               builder.append("</td></tr></table>");
               this.content = builder.toString();
               this.setContent(this.content, ad.getWidth(), ad.getHeight());
               return true;
            default:
               U.log("Unknown ad type:", ad.getType());
               return false;
            }
         }
      }
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
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$updater$AdParser$AdType() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$updater$AdParser$AdType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[AdParser.AdType.values().length];

         try {
            var0[AdParser.AdType.DEFAULT.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$ru$turikhay$tlauncher$updater$AdParser$AdType = var0;
         return var0;
      }
   }

   private class InfoPanelAnimator extends ExtendedThread {
      private AnimatorAction currentAction;
      // $FF: synthetic field
      private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AnimatorAction;

      InfoPanelAnimator() {
         this.startAndWait();
      }

      void act(AnimatorAction action) {
         if (action == null) {
            throw new NullPointerException("action");
         } else {
            this.currentAction = action;
            if (this.isThreadLocked()) {
               this.unlockThread("start");
            }

         }
      }

      public void run() {
         this.lockThread("start");

         while(true) {
            while(this.currentAction == null) {
               U.sleepFor(100L);
            }

            AnimatorAction action = this.currentAction;
            switch($SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AnimatorAction()[action.ordinal()]) {
            case 1:
               InfoPanel.this.show(true);
               break;
            case 2:
               InfoPanel.this.hide(true);
               break;
            default:
               throw new RuntimeException("unknown action: " + this.currentAction);
            }

            if (this.currentAction == action) {
               this.currentAction = null;
            }
         }
      }

      // $FF: synthetic method
      static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AnimatorAction() {
         int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AnimatorAction;
         if (var10000 != null) {
            return var10000;
         } else {
            int[] var0 = new int[AnimatorAction.values().length];

            try {
               var0[AnimatorAction.HIDE.ordinal()] = 2;
            } catch (NoSuchFieldError var2) {
            }

            try {
               var0[AnimatorAction.SHOW.ordinal()] = 1;
            } catch (NoSuchFieldError var1) {
            }

            $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$AnimatorAction = var0;
            return var0;
         }
      }
   }
}
