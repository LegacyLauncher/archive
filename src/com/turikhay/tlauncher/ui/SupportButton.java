package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.U;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import net.minecraft.launcher_.OperatingSystem;

public class SupportButton extends ImageButton {
   private static final long serialVersionUID = 7903730373496194592L;
   private final SupportButton instance = this;
   private final LoginForm lf;
   private final Settings l;
   private final String path;
   private final URL url;
   private final URI uri;

   SupportButton(LoginForm loginform) {
      this.lf = loginform;
      this.l = this.lf.l;
      this.image = loadImage("vk.png");
      this.rotation = ImageButton.ImageRotation.LEFT;
      this.path = this.l.get("support.url");
      this.url = makeURL(this.path);
      this.uri = makeURI(this.url);
      this.setLabel(this.l.getRand("support.label"));
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SupportButton.this.instance.openURL();
            SupportButton.this.lf.defocus();
         }
      });
   }

   public void openURL() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            if (!OperatingSystem.openLink(SupportButton.this.uri)) {
               Alert.showError(SupportButton.this.l.get("support.error.title"), SupportButton.this.l.get("support.error"), (Object)SupportButton.this.path);
            }

         }
      });
   }

   private static URL makeURL(String p) {
      try {
         return new URL(p);
      } catch (Exception var2) {
         U.log("Cannot make URL from string: " + p + ". Check out lang.ini", (Throwable)var2);
         return null;
      }
   }

   private static URI makeURI(URL url) {
      try {
         return url.toURI();
      } catch (Exception var2) {
         U.log("Cannot make URI from URL: " + url + ". Check out lang.ini", (Throwable)var2);
         return null;
      }
   }
}
