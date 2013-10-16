package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.util.U;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher_.updater.VersionSyncInfo;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener {
   private static final long serialVersionUID = 6768252827144456302L;
   private final String LAUNCH_BLOCK = "launch";
   private final String REFRESH_BLOCK = "refresh";
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
                  Alert.showError("versions.notfound");
               }

            } else {
               VersionSyncInfo syncInfo = this.versionchoice.getSyncVersionInfo();
               boolean supporting = syncInfo.isOnRemote();
               boolean installed = syncInfo.isInstalled();
               if (this.checkbox.getForceUpdate()) {
                  if (!supporting) {
                     Alert.showWarning("forceupdate.onlylibraries");
                  } else if (installed && !Alert.showQuestion("forceupdate.question", true)) {
                     return;
                  }
               }

               this.save();
               this.listenerOnLogin();
               this.t.launch(this, this.checkbox.getForceUpdate());
            }
         }
      }
   }

   public void cancelLogin() {
      this.defocus();
      U.log("cancellogin");
      this.unblock("launch");
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
      this.block("launch");
   }

   public void onMinecraftPrepare() {
      this.f.mc.suspendBackground();
   }

   public void onMinecraftLaunch() {
      this.unblock("launch");
      this.listenerOnSuccess();
      this.t.hide();
      this.versionchoice.asyncRefresh();
   }

   public void onMinecraftClose() {
      this.unblock("launch");
      this.t.show();
      this.f.mc.startBackground();
      if (this.autologin.enabled) {
         this.t.getUpdater().asyncFindUpdate();
      }

   }

   public void onMinecraftError(Throwable e) {
      Alert.showError(this.l.get("launcher.error.title"), this.l.get("launcher.error.unknown"), e);
      this.handleError();
   }

   public void onMinecraftError(String message) {
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(message));
      this.handleError();
   }

   public void onMinecraftError(MinecraftLauncherException knownError) {
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(knownError.getLangpath()), knownError.getReplace());
      this.handleError();
   }

   private void handleError() {
      this.unblock("launch");
      this.listenerOnFail();
      this.t.show();
      this.f.mc.startBackground();
   }

   public void onMinecraftWarning(String langpath, Object replace) {
      Alert.showWarning(this.l.get("launcher.warning.title"), this.l.get("launcher.warning." + langpath, "r", replace));
   }

   public void updateLocale() {
      super.updateLocale();
      TLauncherFrame.updateContainer(this, true);
   }
}
