package com.turikhay.tlauncher.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RefreshButton extends ImageButton {
   private static final long serialVersionUID = -1334187593288746348L;
   public static final int TYPE_REFRESH = 0;
   private final LoginForm lf;
   private int type;
   private final Image refresh;

   RefreshButton(LoginForm loginform, int type) {
      this.refresh = loadImage("refresh.png");
      this.lf = loginform;
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.setType(type, false);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            RefreshButton.this.onPressButton();
         }
      });
   }

   RefreshButton(LoginForm loginform) {
      this(loginform, 0);
   }

   private void onPressButton() {
      switch(this.type) {
      case 0:
         this.lf.versionchoice.asyncRefresh();
         this.lf.defocus();
         return;
      default:
         throw new IllegalArgumentException("Unknown type: " + this.type + ". Use RefreshButton.TYPE_* constants.");
      }
   }

   public void setType(int type) {
      this.setType(type, true);
   }

   public void setType(int type, boolean repaint) {
      switch(type) {
      case 0:
         this.image = this.refresh;
         this.type = type;
         if (repaint) {
            this.paint(this.getGraphics());
         }

         return;
      default:
         throw new IllegalArgumentException("Unknown type: " + type + ". Use RefreshButton.TYPE_* constants.");
      }
   }
}
