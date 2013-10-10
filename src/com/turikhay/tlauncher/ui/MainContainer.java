package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Panel;

public class MainContainer extends DecoratedPanel implements LocalizableComponent, MinecraftLauncherListener {
   private static final long serialVersionUID = 8925486339442046362L;
   TLauncherFrame f;
   final MainContainer instance = this;
   final LightBackground bg;
   Font font;

   MainContainer(TLauncherFrame f) {
      this.f = f;
      this.font = f.getFont();
      this.bg = (LightBackground)(f.global.getBoolean("gui.sun") ? new DayBackground(this, -1.0D, false) : new LightBackground(this, -1.0D));
      this.setBackground(f.bgcolor);
      this.setPanelBackground(this.bg);
      GridBagLayout gbl = new GridBagLayout();
      this.setLayout(gbl);
      this.add(f.lf);
      this.startBackground();
   }

   public boolean startBackground() {
      if (!(this.bg instanceof AnimatedBackground)) {
         return false;
      } else {
         ((AnimatedBackground)this.bg).start();
         return true;
      }
   }

   public boolean stopBackground() {
      if (!(this.bg instanceof AnimatedBackground)) {
         return false;
      } else {
         ((AnimatedBackground)this.bg).stop();
         return true;
      }
   }

   public boolean suspendBackground() {
      if (!(this.bg instanceof AnimatedBackground)) {
         return false;
      } else {
         ((AnimatedBackground)this.bg).suspend();
         return true;
      }
   }

   private void setContent(Panel pan) {
      this.removeAll();
      this.add(pan);
      this.validate();
   }

   public void showLogin() {
      this.setContent(this.f.lf);
   }

   public void showSettings() {
      this.setContent(this.f.sf);
   }

   public void update(Graphics g) {
      this.paint(g);
   }

   public void updateLocale() {
      this.f.lf.updateLocale();
      this.f.sf.updateLocale();
   }

   public void onMinecraftCheck() {
   }

   public void onMinecraftPrepare() {
   }

   public void onMinecraftWarning(String langpath, Object replace) {
   }

   public void onMinecraftLaunch() {
      this.stopBackground();
   }

   public void onMinecraftClose() {
      this.startBackground();
   }

   public void onMinecraftError(MinecraftLauncherException knownError) {
      this.startBackground();
   }

   public void onMinecraftError(Throwable unknownError) {
      this.startBackground();
   }
}
