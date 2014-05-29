package ru.turikhay.tlauncher.ui.info;

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
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.EditorPane;
import ru.turikhay.tlauncher.updater.Ad;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.U;

public class InfoPanel extends CenterPanel implements ResizeableComponent, UpdaterListener {
   private static final long serialVersionUID = 3310876991994323902L;
   private static final int MARGIN = 20;
   private final EditorPane browser;
   private final DefaultScene parent;
   private final Object animationLock = new Object();
   private final int timeFrame = 5;
   private float opacity;
   private boolean shown;
   private String content;
   private int width;
   private int height;

   public InfoPanel(DefaultScene parent) {
      super(CenterPanel.tipTheme, new Insets(5, 10, 5, 10));
      this.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
            if (!InfoPanel.this.onClick()) {
               e.consume();
            }

         }

         public void mousePressed(MouseEvent e) {
         }

         public void mouseReleased(MouseEvent e) {
         }

         public void mouseEntered(MouseEvent e) {
         }

         public void mouseExited(MouseEvent e) {
         }
      });
      this.parent = parent;
      this.browser = new EditorPane();
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
         int y = loginFormLocation.y + loginFormSize.height + 20;
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
      if (this.content != null) {
         this.onResize();
         if (!this.shown) {
            synchronized(this.animationLock) {
               this.setVisible(true);
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
      this.show(true);
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
            if (!animate) {
               this.opacity = 0.0F;
            }

            this.shown = false;
         }
      }
   }

   public void hide() {
      this.hide(true);
   }

   public void setShown(boolean shown, boolean animate) {
      if (shown) {
         this.show(animate);
      } else {
         this.hide(animate);
      }

   }

   boolean onClick() {
      return this.shown;
   }

   public void onUpdaterRequesting(Updater u) {
      this.hide();
   }

   public void onUpdaterRequestError(Updater u) {
   }

   public void onUpdateFound(Update upd) {
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onAdFound(Updater u, Ad ad) {
      int[] size = ad.getSize();
      String content = "<table width=\"" + size[0] + "\" height=\"" + size[1] + "\"" + "align=\"center\"><tr><td>";
      if (ad.getImage() != null) {
         content = content + "<img src=\"" + ad.getImage().toExternalForm() + "\" /></td><td>";
      }

      content = content + ad.getContent();
      content = content + "</td></tr></table>";
      this.content = content;
      this.setContent(content, size[0], size[1]);
      if (!this.parent.isSettingsShown()) {
         this.show();
      }

   }
}
