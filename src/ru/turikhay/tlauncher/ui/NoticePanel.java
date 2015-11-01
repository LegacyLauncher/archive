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
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.AnimatorAction;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.editor.ExtendedHTMLEditorKit;
import ru.turikhay.tlauncher.ui.swing.editor.ServerHyperlinkProcessor;
import ru.turikhay.tlauncher.updater.Notices;
import ru.turikhay.tlauncher.updater.Stats;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public class NoticePanel extends CenterPanel implements ResizeableComponent, UpdaterListener, LocalizableComponent {
   private static final int MARGIN = 10;
   private static final float FONT_SIZE = SwingUtil.magnify(12.0F);
   private final NoticePanel.InfoPanelAnimator animator = new NoticePanel.InfoPanelAnimator();
   private final EditorPane browser;
   private final DefaultScene parent;
   private final Object animationLock = new Object();
   private final int timeFrame = 5;
   private float opacity;
   private boolean shown;
   private boolean canshow;
   private Notices ads;
   private String content;
   private int width;
   private int height;
   private int mouseX;
   private int mouseY;
   private Notices.Notice notice;

   public NoticePanel(DefaultScene p) {
      super(CenterPanel.tipTheme, new MagnifiedInsets(5, 10, 5, 10));
      this.parent = p;
      this.browser = new EditorPane(this.getFont().deriveFont(FONT_SIZE));
      if (this.browser.getEditorKit() instanceof ExtendedHTMLEditorKit) {
         ((ExtendedHTMLEditorKit)this.browser.getEditorKit()).setHyperlinkProcessor(new ServerHyperlinkProcessor() {
            public void showPopup(JPopupMenu menu) {
               menu.show(NoticePanel.this.browser, NoticePanel.this.mouseX, (int)((float)NoticePanel.this.mouseY + NoticePanel.FONT_SIZE));
            }

            public void open(VersionSyncInfo vs, ServerList.Server server) {
               LoginForm lf = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm;
               if (vs != null) {
                  lf.versions.setSelectedValue(vs);
                  if (!vs.equals(lf.versions.getSelectedValue())) {
                     return;
                  }
               }

               Account account = (Account)lf.accounts.getSelectedValue();
               if (account != null && !server.isAccountTypeAllowed(account.getType())) {
                  List allowedList = server.getAllowedAccountTypeList();
                  String message;
                  if (allowedList.size() == 1) {
                     message = Localizable.get("ad.server.choose-account", Localizable.get("account.type." + allowedList.get(0)));
                  } else {
                     StringBuilder messageBuilder = (new StringBuilder(Localizable.get("ad.server.choose-account.multiple"))).append('\n');
                     Iterator var9 = allowedList.iterator();

                     while(var9.hasNext()) {
                        Account.AccountType type = (Account.AccountType)var9.next();
                        messageBuilder.append(Localizable.get("ad.server.choose-account.multiple.prefix", Localizable.get("account.type." + type))).append('\n');
                     }

                     message = messageBuilder.substring(0, messageBuilder.length() - 1);
                  }

                  lf.scene.getMainPane().openAccountEditor();
                  Alert.showError(Localizable.get("ad.server.choose-account.title"), message);
               } else {
                  lf.startLauncher(server);
               }

            }
         });
      }

      this.browser.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
            if (!NoticePanel.this.onClick()) {
               e.consume();
            }

         }

         public void mousePressed(MouseEvent e) {
            if (!NoticePanel.this.isVisible()) {
               e.consume();
            }

            NoticePanel.this.mouseX = e.getX();
            NoticePanel.this.mouseY = e.getY();
         }

         public void mouseReleased(MouseEvent e) {
            if (!NoticePanel.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseEntered(MouseEvent e) {
            if (!NoticePanel.this.isVisible()) {
               e.consume();
            }

         }

         public void mouseExited(MouseEvent e) {
            if (!NoticePanel.this.isVisible()) {
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
         switch(this.parent.getLoginFormDirection()) {
         case TOP_LEFT:
         case TOP:
         case TOP_RIGHT:
         case CENTER_LEFT:
         case CENTER:
         case CENTER_RIGHT:
            y = loginFormLocation.y + loginFormSize.height + 10;
            break;
         case BOTTOM_LEFT:
         case BOTTOM:
         case BOTTOM_RIGHT:
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
            Object var2 = this.animationLock;
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
                     U.sleepFor(5L);
                  }
               } else {
                  this.opacity = selectedOpacity;
                  this.repaint();
               }

               this.shown = true;
            }

            if (this.notice != null) {
               Stats.noticeViewed(this.notice);
            }
         }
      }

   }

   public void show() {
      this.animator.act(AnimatorAction.SHOW);
   }

   void hide(boolean animate) {
      if (this.shown) {
         Object var2 = this.animationLock;
         synchronized(this.animationLock) {
            if (animate) {
               while(this.opacity > 0.0F) {
                  this.opacity -= 0.01F;
                  if (this.opacity < 0.0F) {
                     this.opacity = 0.0F;
                  }

                  this.repaint();
                  U.sleepFor(5L);
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
      this.ads = succeeded.getResponse().getNotices();
      this.updateNotice(true);
   }

   public void updateNotice(boolean animate) {
      this.hide(animate);
      this.canshow = this.prepareNotice();
      if (this.parent.getSidePanel() != DefaultScene.SidePanel.SETTINGS) {
         this.show(animate);
      }

   }

   private boolean prepareNotice() {
      if (this.ads == null) {
         this.notice = null;
         return false;
      } else {
         String locale = this.parent.getMainPane().getRootFrame().getLauncher().getSettings().getLocale().toString();
         Notices.NoticeList noticeList = this.ads.getByName(locale);
         if (noticeList != null && !noticeList.getList().isEmpty()) {
            this.notice = noticeList.getRandom();
            if (this.notice == null) {
               return false;
            } else {
               boolean isAllowed = !this.notice.getType().isAdvert() || this.tlauncher.getSettings().getBoolean("gui.notice." + this.notice.getType().name().toLowerCase());
               if (!isAllowed) {
                  return false;
               } else {
                  StringBuilder builder = new StringBuilder();
                  int width = (int)((double)this.notice.getWidth() * TLauncherFrame.magnifyDimensions);
                  int height = (int)((double)this.notice.getHeight() * TLauncherFrame.magnifyDimensions);
                  builder.append("<table width=\"").append(width).append("\" height=\"").append(height).append("\"><tr><td align=\"center\" valign=\"center\">");
                  if (this.notice.getImage() != null) {
                     builder.append("<img src=\"").append(this.notice.getImage()).append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");
                  }

                  builder.append(this.notice.getContent());
                  builder.append("</td></tr></table>");
                  this.content = builder.toString();
                  this.setContent(this.content, width, height);
                  return true;
               }
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
      this.updateNotice(false);
   }

   private class InfoPanelAnimator extends ExtendedThread {
      private AnimatorAction currentAction;

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
            while(this.currentAction != null) {
               AnimatorAction action = this.currentAction;
               switch(action) {
               case SHOW:
                  NoticePanel.this.show(true);
                  break;
               case HIDE:
                  NoticePanel.this.hide(true);
                  break;
               default:
                  throw new RuntimeException("unknown action: " + this.currentAction);
               }

               if (this.currentAction == action) {
                  this.currentAction = null;
               }
            }

            U.sleepFor(100L);
         }
      }
   }
}
