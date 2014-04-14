package com.turikhay.tlauncher.ui.login.buttons;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.swing.ImageButton;
import com.turikhay.util.OS;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;

public class SupportButton extends ImageButton implements Blockable, LocalizableComponent {
   private static final long serialVersionUID = 7903730373496194592L;
   private final SupportButton instance = this;
   private final LoginForm lf;
   private final LangConfiguration l;
   private URI uri;
   private final Image vk = loadImage("vk.png");
   private final Image mail = loadImage("mail.png");

   SupportButton(LoginForm loginform) {
      this.lf = loginform;
      this.l = this.lf.lang;
      this.image = this.selectImage();
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SupportButton.this.instance.openURL();
            SupportButton.this.lf.defocus();
         }
      });
      this.updateURL();
      this.initImage();
   }

   void openURL() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            OS.openLink(SupportButton.this.uri);
         }
      });
   }

   private Image selectImage() {
      String locale = TLauncher.getInstance().getSettings().getLocale().toString();
      return !locale.equals("ru_RU") && !locale.equals("uk_UA") ? this.mail : this.vk;
   }

   private void updateURL() {
      String path = this.l.nget("support.url");
      URL url = U.makeURL(path);
      this.uri = U.makeURI(url);
   }

   public void updateLocale() {
      this.image = this.selectImage();
      this.updateURL();
   }

   public void block(Object reason) {
   }

   public void unblock(Object reason) {
   }
}
