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
import net.minecraft.launcher.updater.VersionSyncInfo;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.exceptions.ParseException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.AnimatorAction;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.editor.ExtendedHTMLEditorKit;
import ru.turikhay.tlauncher.ui.swing.editor.HyperlinkProcessor;
import ru.turikhay.tlauncher.updater.Ads;
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
   private Ads ads;
   private String content;
   private int width;
   private int height;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$util$Direction;

   public InfoPanel(DefaultScene p) {
      super(CenterPanel.tipTheme, new Insets(5, 10, 5, 10));
      this.parent = p;
      this.browser = new EditorPane(this.getFont().deriveFont(12.0F));
      if (this.browser.getEditorKit() instanceof ExtendedHTMLEditorKit) {
         ((ExtendedHTMLEditorKit)this.browser.getEditorKit()).setHyperlinkProcessor(new HyperlinkProcessor() {
            public void process(String link) {
               if (link != null && link.startsWith("server:")) {
                  if (!Blocker.isBlocked(InfoPanel.this.parent.loginForm)) {
                     try {
                        this.openServer(link);
                     } catch (Exception var3) {
                        Alert.showLocError("ad.server.error", new RuntimeException("link: \"" + link + "\"", var3));
                     }
                  }

               } else {
                  ExtendedHTMLEditorKit.defaultHyperlinkProcessor.process(link);
               }
            }

            private void openServer(String link) throws ParseException {
               String[] info = StringUtils.split(link.substring("server:".length()), ';');
               if (info.length != 4) {
                  throw new ParseException("split incorrectly");
               } else if (StringUtils.isEmpty(info[0])) {
                  throw new ParseException("ip is not defined");
               } else if (StringUtils.isEmpty(info[1])) {
                  throw new ParseException("port is not defined");
               } else {
                  Integer.parseInt(info[1]);
                  if (StringUtils.isEmpty(info[2])) {
                     throw new ParseException("version is not defined");
                  } else if (StringUtils.isEmpty(info[3])) {
                     throw new ParseException("name is not defined");
                  } else {
                     VersionManager vm = TLauncher.getInstance().getVersionManager();
                     VersionSyncInfo versionSync = vm.getVersionSyncInfo(info[2]);
                     if (versionSync == null) {
                        throw new IllegalArgumentException("cannot find version: " + info[2]);
                     } else {
                        LoginForm lf = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm;
                        lf.versions.setSelectedValue(versionSync);
                        if (!versionSync.equals(lf.versions.getSelectedItem())) {
                           throw new RuntimeException("cannot select version: " + versionSync);
                        } else {
                           ServerList.Server server = new ServerList.Server();
                           server.setName(info[3]);
                           server.setVersion(info[2]);
                           server.setAddress(info[0] + ':' + info[1]);
                           lf.startLauncher(server);
                        }
                     }
                  }
               }
            }
         });
      }

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
      return this.isEnabled() && this.shown;
   }

   public void onUpdaterRequesting(Updater u) {
      this.hide(true);
   }

   public void onUpdaterErrored(Updater.SearchFailed failed) {
   }

   public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
      this.ads = succeeded.getResponse().getAds();
      this.updateAd(true);
   }

   public void updateAd(boolean animate) {
      this.hide(animate);
      this.canshow = this.prepareAd();
      if (this.parent.getSidePanel() != DefaultScene.SidePanel.SETTINGS) {
         this.show(animate);
      }

   }

   private boolean prepareAd() {
      if (this.ads == null) {
         return false;
      } else {
         String locale = this.parent.getMainPane().getRootFrame().getLauncher().getSettings().getLocale().toString();
         Ads.AdList adList = this.ads.getByName(locale);
         if (adList != null && !adList.getAds().isEmpty()) {
            Ads.Ad ad = adList.getRandom();
            if (ad == null) {
               return false;
            } else {
               StringBuilder builder = new StringBuilder();
               builder.append("<table width=\"").append(ad.getWidth()).append("\" height=\"").append(ad.getHeight()).append("\"><tr><td align=\"center\" valign=\"center\">");
               if (ad.getImage() != null) {
                  builder.append("<img src=\"").append(ad.getImage()).append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");
               }

               builder.append(ad.getContent());
               builder.append("</td></tr></table>");
               this.content = builder.toString();
               this.setContent(this.content, ad.getWidth(), ad.getHeight());
               return true;
            }
         } else {
            return false;
         }
      }
   }

   public void block(Object reason) {
   }

   public void unblock(Object reason) {
   }

   public void updateLocale() {
      this.updateAd(false);
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
