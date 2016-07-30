package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.frames.ProcessFrame;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.support.PreSupportFrame;
import ru.turikhay.util.DXDiagScanner;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class SupportButton extends LocalizableButton implements Blockable {
   private ProcessFrame dxdiagFlusher = new ProcessFrame() {
      {
         this.setTitlePath("loginform.button.support.processing.title", new Object[0]);
         this.getHead().setText("loginform.button.support.processing.head");
         this.setIcon("consulting.png");
         this.pack();
      }

      protected void onSucceeded(ProcessFrame.Process process, Void result) {
         super.onSucceeded(process, result);
         SupportButton.this.supportFrame.showAtCenter();
      }
   };
   private PreSupportFrame supportFrame = new PreSupportFrame();
   private final ActionListener showSupportFrame = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         if (!SupportButton.this.supportFrame.isVisible()) {
            ProcessFrame var10000 = SupportButton.this.dxdiagFlusher;
            ProcessFrame var10004 = SupportButton.this.dxdiagFlusher;
            var10004.getClass();
            var10000.submit(new ProcessFrame.Process(var10004) {
               {
                  x0.getClass();
               }

               protected Void get() throws Exception {
                  if (DXDiagScanner.isScannable()) {
                     U.log("<DXDiag>");

                     try {
                        DXDiagScanner.DXDiagScannerResult result = DXDiagScanner.getInstance().getResult();
                        Iterator var2 = result.getLines().iterator();

                        while(var2.hasNext()) {
                           String l = (String)var2.next();
                           U.log(l);
                        }

                        U.log("In a nutshell:");
                        U.log("System info:", result.getSystemInfo());
                        U.log("Display devices:", result.getDisplayDevices());
                     } catch (DXDiagScanner.DXDiagException var4) {
                        U.log("Could not fetch DXDiag info:", var4);
                     } catch (InterruptedException var5) {
                        U.log("Interrupted", var5);
                     }

                     U.log("</DXDiag>");
                  }

                  return null;
               }
            });
         }
      }
   };
   private final HashMap localeMap = new HashMap();
   SupportButton.SupportMenu menu;

   SupportButton(LoginForm loginForm) {
      this.localeMap.put("ru_RU", (new SupportButton.SupportMenu("vk.png")).add("loginform.button.support.vk", Images.getIcon("vk.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/vk?from=menu")).addSeparator().add("loginform.button.support", Images.getIcon("consulting.png", SwingUtil.magnify(16)), this.showSupportFrame));
      this.localeMap.put("uk_UA", this.localeMap.get("ru_RU"));
      this.localeMap.put("en_US", (new SupportButton.SupportMenu("mail.png")).add("loginform.button.support.fb", Images.getIcon("facebook.png", SwingUtil.magnify(16)), actionURL("http://tlaun.ch/fb?from=menu")).addSeparator().add("loginform.button.support", Images.getIcon("consulting.png", SwingUtil.magnify(16)), this.showSupportFrame));
      this.setToolTipText("loginform.button.support");
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SupportButton.this.menu != null) {
               SupportButton.this.menu.popup.show(SupportButton.this, 0, SupportButton.this.getHeight());
            }

         }
      });
      this.updateLocale();
   }

   void setLocale(String locale) {
      if (this.menu != null) {
         this.menu.popup.setVisible(false);
      }

      this.menu = (SupportButton.SupportMenu)this.localeMap.get(locale);
      if (this.menu == null) {
         this.setIcon((Icon)null);
         this.setEnabled(false);
      } else {
         this.setIcon(this.menu.icon);
         this.setEnabled(true);
      }

   }

   public void block(Object reason) {
   }

   public void unblock(Object reason) {
   }

   public void updateLocale() {
      super.updateLocale();
      PreSupportFrame oldSupportFrame = this.supportFrame;
      this.supportFrame = new PreSupportFrame();
      if (oldSupportFrame.isVisible()) {
         oldSupportFrame.dispose();
         this.supportFrame.showAtCenter();
      }

      String selectedLocale = TLauncher.getInstance().getSettings().getLocale().toString();
      String newLocale = "en_US";
      Iterator var4 = this.localeMap.keySet().iterator();

      while(var4.hasNext()) {
         String locale = (String)var4.next();
         if (locale.equals(selectedLocale)) {
            newLocale = locale;
            break;
         }
      }

      this.setLocale(newLocale);
   }

   private static ActionListener actionURL(String rawURL) {
      final URL tryURL;
      try {
         tryURL = new URL(rawURL);
      } catch (MalformedURLException var3) {
         throw new RuntimeException(var3);
      }

      return new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            OS.openLink(tryURL);
         }
      };
   }

   private class SupportMenu {
      final ImageIcon icon;
      final JPopupMenu popup = new JPopupMenu();

      SupportMenu(String icon) {
         this.icon = Images.getScaledIcon(icon, 16);
      }

      SupportButton.SupportMenu add(JMenuItem item) {
         this.popup.add(item);
         return this;
      }

      public SupportButton.SupportMenu add(String key, ImageIcon icon, ActionListener listener) {
         LocalizableMenuItem item = new LocalizableMenuItem(key);
         item.setIcon(icon);
         if (listener != null) {
            item.addActionListener(listener);
         }

         this.add(item);
         return this;
      }

      public SupportButton.SupportMenu addSeparator() {
         this.popup.addSeparator();
         return this;
      }
   }
}
