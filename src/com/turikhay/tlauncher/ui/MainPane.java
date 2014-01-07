package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import javax.swing.JLayeredPane;

public class MainPane extends JLayeredPane implements MinecraftLauncherListener {
   private static final long serialVersionUID = 8925486339442046362L;
   TLauncherFrame f;
   final Integer BACKGROUND_PANEL;
   final Integer LOGINFORM;
   final Integer SETTINGSFORM;
   final Integer PROFILECREATOR;
   final Integer CONNECTIONWARNING;
   final MainPane instance = this;
   final DecoratedPanel bgpan;
   final Background bg;
   final LoginForm lf;
   final SettingsForm sf;
   final ProfileCreatorForm spcf;
   final ConnectionWarning warning;
   private boolean settings;

   MainPane(TLauncherFrame f) {
      this.f = f;
      this.lf = f.lf;
      this.sf = f.sf;
      this.spcf = f.spcf;
      this.warning = f.warning;
      int i = -1;
      int i = i + 1;
      this.BACKGROUND_PANEL = i;
      ++i;
      this.LOGINFORM = i;
      ++i;
      this.SETTINGSFORM = i;
      ++i;
      this.PROFILECREATOR = i;
      ++i;
      this.CONNECTIONWARNING = i;
      this.bgpan = new DecoratedPanel();
      this.bg = this.chooseBackground();
      this.bgpan.setPanelBackground(this.bg);
      this.add(this.bgpan, this.BACKGROUND_PANEL);
      this.add(this.lf, this.LOGINFORM);
      this.add(this.sf, this.SETTINGSFORM);
      this.add(this.warning, this.CONNECTIONWARNING);
      this.startBackground();
   }

   public void onResize() {
      this.bgpan.setBounds(0, 0, this.getWidth(), this.getHeight());
      this.lf.setSize(250, 250);
      this.sf.setSize(500, 500);
      this.warning.setLocation(5, 5);
      this.setSettings(this.settings, false);
   }

   private Background chooseBackground() {
      return (Background)(this.f.global.getBoolean("gui.sun") ? new DayBackground(this.bgpan, -1.0D) : new LightBackground(this.bgpan, -1.0D));
   }

   public boolean startBackground() {
      if (!(this.bg instanceof AnimatedBackground)) {
         return false;
      } else {
         AnimatedBackground abg = (AnimatedBackground)this.bg;
         if (!abg.isAllowed()) {
            return false;
         } else {
            abg.start();
            return true;
         }
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

   public void setSettings(boolean shown, boolean animate) {
      if (this.settings != shown || !animate) {
         if (shown) {
            this.sf.unblockElement("");
         } else {
            this.sf.blockElement("");
         }

         this.sf.updateIfSaveable();
         int w = this.getWidth();
         int h = this.getHeight();
         int hw = w / 2;
         int hh = h / 2;
         int lf_x;
         int lf_y;
         int sf_x;
         int sf_y;
         if (shown) {
            int margin = 15;
            int bw = 750 + margin;
            int hbw = bw / 2;
            lf_x = hw - hbw;
            lf_y = hh - 125;
            sf_x = hw - hbw + 250 + margin;
            sf_y = hh - 250;
         } else {
            lf_x = hw - 125;
            lf_y = hh - 125;
            sf_x = w;
            sf_y = hh - 250;
         }

         AnimateThread.animate(this.lf, lf_x, lf_y);
         AnimateThread.animate(this.sf, sf_x, sf_y);
         this.settings = shown;
      }
   }

   public void setSettings(boolean shown) {
      this.setSettings(shown, true);
   }

   public void toggleSettings() {
      this.setSettings(!this.settings);
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

   public void onMinecraftLaunchStop() {
      this.startBackground();
   }

   public void onMinecraftClose() {
      this.startBackground();
   }

   public void onMinecraftKnownError(MinecraftLauncherException knownError) {
      this.startBackground();
   }

   public void onMinecraftError(Throwable unknownError) {
      this.startBackground();
   }

   public void onMinecraftCrash(Crash crash) {
      this.startBackground();
   }
}
