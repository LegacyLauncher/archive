package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.util.U;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener {
   private static final long serialVersionUID = 6768252827144456302L;
   final LoginForm instance = this;
   public final MainInputPanel maininput;
   public final VersionChoicePanel versionchoice;
   public final CheckBoxPanel checkbox;
   public final ButtonPanel buttons;
   public final Autologin autologin;

   LoginForm(TLauncherFrame fd) {
      super(fd);
      String username = this.s.get("login.username");
      String version = this.s.get("login.version");
      boolean autologin = this.s.getBoolean("login.auto");
      boolean console = this.s.getBoolean("login.debug");
      int timeout = this.s.getInteger("login.auto.timeout");
      this.autologin = new Autologin(this, autologin, timeout);
      this.autologin.enabled = false;
      this.maininput = new MainInputPanel(this, username);
      this.versionchoice = new VersionChoicePanel(this, version);
      this.checkbox = new CheckBoxPanel(this, autologin, console);
      this.buttons = new ButtonPanel(this);
      this.add(this.error);
      this.add(this.maininput);
      this.add(this.versionchoice);
      this.add(this.del(1));
      this.add(this.checkbox);
      this.add(this.del(-1));
      this.add(this.buttons);
   }

   private void save() {
      U.log("save");
      this.s.set("login.username", this.maininput.username);
      this.s.set("login.version", this.versionchoice.version);
      this.s.set("login.debug", this.checkbox.console);
   }

   public void callLogin() {
      this.defocus();
      if (!this.isBlocked()) {
         U.log("login");
         if (this.maininput.checkUsername(true)) {
            if (!this.versionchoice.foundlocal) {
               this.block("refresh");
               this.versionchoice.refresh();
               this.unblock("refresh");
               if (!this.versionchoice.foundlocal) {
                  this.setError(this.l.get("versions.notfound"));
               }

            } else {
               this.setError((String)null);
               this.save();
               this.postAutoLogin();
               this.t.launch(this, this.maininput.username, this.versionchoice.version, this.checkbox.forceupdate, this.checkbox.console);
            }
         }
      }
   }

   public void cancelLogin() {
      this.defocus();
      U.log("cancellogin");
      this.unblock("launcher");
   }

   void setAutoLogin(boolean enabled) {
      if (!enabled) {
         this.cancelAutoLogin();
      } else {
         this.autologin.enabled = true;
      }

      this.s.set("login.auto", this.autologin.enabled);
   }

   private void cancelAutoLogin() {
      this.autologin.enabled = false;
      this.autologin.stopLogin();
      this.checkbox.uncheckAutologin();
      this.buttons.toggleSouthButton();
      if (this.autologin.active) {
         this.versionchoice.asyncRefresh();
      }

   }

   private void postAutoLogin() {
      if (this.autologin.enabled) {
         U.log(this.autologin.enabled);
         this.autologin.stopLogin();
         this.autologin.active = false;
         this.buttons.toggleSouthButton();
      }
   }

   public void setError(String message) {
      if (message == null) {
         this.border = this.green;
         this.repaint();
         this.error.setText("");
      } else {
         this.border = this.red;
         this.repaint();
         this.error.setText(message);
      }
   }

   protected void blockElement(Object reason) {
      this.defocus();
      this.maininput.block(reason);
      this.versionchoice.block(reason);
      this.buttons.block(reason);
   }

   protected void unblockElement(Object reason) {
      this.defocus();
      this.maininput.unblock(reason);
      this.versionchoice.unblock(reason);
      this.buttons.unblock(reason);
   }

   public void onMinecraftCheck() {
      this.block("launcher");
   }

   public void onMinecraftPrepare() {
      this.block("launcher");
      this.f.mc.sun.suspend();
   }

   public void onMinecraftLaunch() {
      this.unblock("launcher");
      this.versionchoice.asyncRefresh();
   }

   public void onMinecraftClose() {
      this.unblock("launcher");
      this.f.mc.sun.resume();
   }

   public void onMinecraftError(Throwable e) {
      this.unblock("launcher");
      Alert.showError(this.l.get("launcher.error.title"), this.l.get("launcher.error.unknown"), e);
      this.f.mc.sun.resume();
   }

   public void onMinecraftError(String message) {
      this.unblock("launcher");
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(message));
      this.f.mc.sun.resume();
   }

   public void onMinecraftError(MinecraftLauncherException knownError) {
      this.unblock("launcher");
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(knownError.getLangpath()), knownError.getReplace());
      this.f.mc.sun.resume();
   }

   public void onMinecraftWarning(String langpath, Object replace) {
      Alert.showWarning(this.l.get("launcher.warning.title"), this.l.get("launcher.warning." + langpath, "r", replace));
   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdaterRequestError(Updater u, Throwable e) {
   }

   public void onUpdaterFoundUpdate(Updater u, boolean canBeInstalledAutomatically) {
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onUpdaterDownloading(Updater u) {
   }

   public void onUpdaterDownloadSuccess(Updater u) {
      this.block("updater");
   }

   public void onUpdaterDownloadError(Updater u, Throwable e) {
   }

   public void onUpdaterProcessError(Updater u, Throwable e) {
      this.unblock("updater");
   }
}
