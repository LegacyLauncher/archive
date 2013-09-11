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
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.path = this.l.get("support.url");
      this.url = U.makeURL(this.path);
      this.uri = U.makeURI(this.url);
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
}
