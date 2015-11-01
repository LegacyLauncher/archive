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
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class SupportButton extends ExtendedButton implements Blockable, LocalizableComponent {
   private final HashMap localeMap = new HashMap();
   SupportButton.SupportMenu menu;

   SupportButton(LoginForm loginForm) {
      this.localeMap.put("ru_RU", (new SupportButton.SupportMenu("vk.png")).add("loginform.button.support.follow", actionURL("http://tlauncher.ru/go/vk?from=menu")).add("loginform.button.support.report", actionURL("http://tlauncher.ru/go/report?from=menu")).add("loginform.button.support.author", actionURL("http://tlauncher.ru/go/support?from=menu")));
      this.localeMap.put("uk_UA", this.localeMap.get("ru_RU"));
      this.localeMap.put("en_US", (new SupportButton.SupportMenu("mail.png")).add("loginform.button.support.email", actionAlert("loginform.button.support.email.alert", U.reverse("ur.rehcnualt@troppus"))).add("loginform.button.support.developer", actionAlert("loginform.button.support.developer.alert", U.reverse("ur.rehcnualt@repoleved"))));
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
      String selectedLocale = TLauncher.getInstance().getSettings().getLocale().toString();
      String newLocale = "en_US";
      Iterator i$ = this.localeMap.keySet().iterator();

      while(i$.hasNext()) {
         String locale = (String)i$.next();
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

   private static ActionListener actionAlert(final String msgPath, final Object textArea) {
      return new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            Alert.showLocMessage(msgPath, textArea);
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

         this.add((JMenuItem)item);
         return this;
      }

      public SupportButton.SupportMenu add(String key, ActionListener listener) {
         return this.add(key, (ImageIcon)null, listener);
      }

      public SupportButton.SupportMenu add(String key) {
         LocalizableMenuItem item = new LocalizableMenuItem(key);
         item.setEnabled(false);
         this.add((JMenuItem)item);
         return this;
      }

      public SupportButton.SupportMenu addSeparator() {
         this.popup.addSeparator();
         return this;
      }
   }
}
