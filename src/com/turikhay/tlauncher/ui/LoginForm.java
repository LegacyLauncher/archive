package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.util.U;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener {
   private static final long serialVersionUID = 6768252827144456302L;
   final LoginForm instance = this;
   final SettingsForm settings;
   final List listeners = new ArrayList();
   public final MainInputPanel maininput;
   public final VersionChoicePanel versionchoice;
   public final CheckBoxPanel checkbox;
   public final ButtonPanel buttons;
   public final Autologin autologin;

   LoginForm(TLauncherFrame fd) {
      super(fd);
      this.settings = this.f.sf;
      String username = this.s.get("login.username");
      String version = this.s.get("login.version");
      boolean autologin = this.s.getBoolean("login.auto");
      boolean console = this.s.getBoolean("login.debug");
      int timeout = this.s.getInteger("login.auto.timeout");
      this.autologin = new Autologin(this, autologin, timeout);
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
      U.log("Saving login settings...");
      this.s.set("login.username", this.maininput.field.username);
      this.s.set("login.version", this.versionchoice.version);
      U.log("Login settings saved!");
   }

   public void callLogin() {
      this.defocus();
      if (!this.isBlocked()) {
         U.log("login");
         this.postAutoLogin();
         if (this.maininput.field.check(false)) {
            if (!this.settings.save()) {
               this.f.mc.showSettings();
            } else if (!this.versionchoice.foundlocal) {
               this.block("refresh");
               this.versionchoice.refresh();
               this.unblock("refresh");
               if (!this.versionchoice.foundlocal) {
                  this.setError(this.l.get("versions.notfound"));
               }

            } else {
               this.setError((String)null);
               boolean official = this.versionchoice.getSyncVersionInfo().isOnRemote();
               boolean installed = this.versionchoice.getSyncVersionInfo().isInstalled();
               if (this.checkbox.forceupdate) {
                  if (!official) {
                     Alert.showWarning("forceupdate.onlylibraries");
                  } else if (installed && !Alert.showQuestion("forceupdate.question", true)) {
                     return;
                  }
               }

               this.save();
               this.listenerOnLogin();
               this.t.launch(this, this.checkbox.forceupdate);
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
         Alert.showMessage("loginform.checkbox.autologin.tip", this.l.get("loginform.checkbox.autologin.tip.arg"));
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
         this.autologin.stopLogin();
         this.autologin.active = false;
         this.buttons.toggleSouthButton();
      }
   }

   public void addListener(LoginListener ll) {
      this.listeners.add(ll);
   }

   public void removeListener(LoginListener ll) {
      this.listeners.remove(ll);
   }

   private void listenerOnLogin() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         LoginListener ll = (LoginListener)var2.next();
         ll.onLogin();
      }

   }

   private void listenerOnFail() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         LoginListener ll = (LoginListener)var2.next();
         ll.onLoginFailed();
      }

   }

   private void listenerOnSuccess() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         LoginListener ll = (LoginListener)var2.next();
         ll.onLoginSuccess();
      }

   }

   protected void blockElement(Object reason) {
      this.defocus();
      this.maininput.blockElement(reason);
      this.versionchoice.blockElement(reason);
      this.checkbox.blockElement(reason);
      this.buttons.blockElement(reason);
   }

   protected void unblockElement(Object reason) {
      this.defocus();
      this.maininput.unblockElement(reason);
      this.versionchoice.unblockElement(reason);
      this.checkbox.unblockElement(reason);
      this.buttons.unblockElement(reason);
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
      this.listenerOnSuccess();
      this.versionchoice.asyncRefresh();
   }

   public void onMinecraftClose() {
      this.unblock("launcher");
      this.f.mc.sun.resume();
   }

   public void onMinecraftError(Throwable e) {
      this.handleError();
      Alert.showError(this.l.get("launcher.error.title"), this.l.get("launcher.error.unknown"), e);
   }

   public void onMinecraftError(String message) {
      this.handleError();
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(message));
   }

   public void onMinecraftError(MinecraftLauncherException knownError) {
      this.handleError();
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(knownError.getLangpath()), knownError.getReplace());
   }

   private void handleError() {
      this.listenerOnFail();
      this.unblock("launcher");
      if (this.f.mc.sun.cancelled()) {
         this.f.mc.sun.resume();
      }

   }

   public void onMinecraftWarning(String langpath, Object replace) {
      Alert.showWarning(this.l.get("launcher.warning.title"), this.l.get("launcher.warning." + langpath, "r", replace));
   }
}
