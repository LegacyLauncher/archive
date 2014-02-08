package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.CrashSignature;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.settings.SettingsForm;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.util.U;
import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher.OperatingSystem;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener, AuthenticatorListener, UpdaterListener, UpdateListener {
   private static final long serialVersionUID = 6768252827144456302L;
   final String LAUNCH_BLOCK = "launch";
   final String AUTH_BLOCK = "auth";
   final String UPDATER_BLOCK = "update";
   final LoginForm instance = this;
   final SettingsForm settings;
   final List listeners = new ArrayList();
   final MainPane pane;
   public final DefaultScene scene;
   public final AccountChoicePanel accountchoice;
   public final VersionChoicePanel versionchoice;
   public final CheckBoxPanel checkbox;
   public final ButtonPanel buttons;
   public final Autologin autologin;

   public LoginForm(DefaultScene scene) {
      this.scene = scene;
      this.settings = scene.settingsForm;
      this.pane = scene.getMainPane();
      String account = this.global.get("login.account");
      String version = this.global.get("login.version");
      boolean auto = this.global.getBoolean("login.auto");
      boolean console = this.global.getBoolean("login.debug");
      int timeout = this.global.getInteger("login.auto.timeout");
      this.autologin = new Autologin(this, auto, timeout);
      this.accountchoice = new AccountChoicePanel(this, account);
      this.versionchoice = new VersionChoicePanel(this, version);
      this.checkbox = new CheckBoxPanel(this, auto, console);
      this.buttons = new ButtonPanel(this);
      this.addListener(this.versionchoice);
      this.addListener(this.autologin);
      this.addListener(this.checkbox);
      this.addListener(this.settings);
      this.add(this.messagePanel);
      this.add(sepPan(new Component[]{this.accountchoice, this.versionchoice}));
      this.add(this.del(1));
      this.add(this.checkbox);
      this.add(this.del(0));
      this.add(this.buttons);
      TLauncher.getInstance().getUpdater().addListener(this);
   }

   private void save() {
      U.log("Saving login settings...");
      this.global.set("login.account", this.accountchoice.account.getUsername());
      this.global.set("login.version", this.versionchoice.version);
      U.log("Login settings saved!");
   }

   public void callLogin() {
      if (!Blocker.isBlocked(this)) {
         if (!this.accountchoice.waitOnLogin()) {
            this.runLogin();
         }
      }
   }

   protected void runLogin() {
      this.defocus();
      U.log("Loggining in...");
      if (!this.listenerOnLogin()) {
         U.log("Login cancelled");
      } else {
         this.save();
         this.tlauncher.launch(this, this.checkbox.getForceUpdate());
         this.block("launch");
      }
   }

   public void cancelLogin() {
      this.defocus();
      U.log("cancellogin");
      this.unblock("launch");
   }

   void setAutoLogin(boolean enabled) {
      if (!enabled) {
         this.autologin.cancel();
      } else {
         Alert.showAsyncMessage("loginform.checkbox.autologin.tip", this.lang.get("loginform.checkbox.autologin.tip.arg"));
         this.autologin.enabled = true;
      }

      this.global.set("login.auto", this.autologin.enabled);
   }

   private void addListener(LoginListener ll) {
      this.listeners.add(ll);
   }

   private boolean listenerOnLogin() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         LoginListener ll = (LoginListener)var2.next();
         U.log("onLogin: ", ll.getClass().getSimpleName());
         if (!ll.onLogin()) {
            return false;
         }
      }

      return true;
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

   public void block(Object reason) {
      this.defocus();
      if (!reason.equals("version_refresh")) {
         Blocker.block((Blockable)this.accountchoice, (Object)reason);
      }

      Blocker.block(reason, this.versionchoice, this.checkbox, this.buttons, this.settings);
   }

   public void unblock(Object reason) {
      this.defocus();
      Blocker.unblock(reason, this.accountchoice, this.versionchoice, this.checkbox, this.buttons, this.settings);
   }

   public void onMinecraftCheck() {
      this.block("launch");
   }

   public void onMinecraftPrepare() {
   }

   public void onMinecraftLaunch() {
      this.unblock("launch");
      this.listenerOnSuccess();
      this.tlauncher.hide();
      this.versionchoice.asyncRefresh();
      if (this.autologin.enabled) {
         this.tlauncher.getUpdater().asyncFindUpdate();
      }

   }

   public void onMinecraftLaunchStop() {
      this.handleError();
   }

   public void onMinecraftClose() {
      this.unblock("launch");
      this.tlauncher.show();
      this.tlauncher.getUpdaterListener().applyDelayedUpdate();
   }

   public void onMinecraftError(Throwable e) {
      Alert.showError(this.lang.get("launcher.error.title"), this.lang.get("launcher.error.unknown"), e);
      this.handleError();
   }

   public void onMinecraftKnownError(MinecraftLauncherException knownError) {
      Alert.showError(this.lang.get("launcher.error.title"), this.lang.get(knownError.getLangpath()), knownError.getReplace());
      this.handleError();
   }

   private void handleError() {
      this.unblock("launch");
      this.listenerOnFail();
      this.tlauncher.show();
   }

   public void onMinecraftWarning(String langpath, Object replace) {
      Alert.showWarning(this.lang.get("launcher.warning.title"), this.lang.get("launcher.warning." + langpath, replace));
   }

   public void onMinecraftCrash(Crash crash) {
      String p = "crash.";
      String title = this.lang.get(p + "title");
      String report = crash.getFile();
      if (!crash.isRecognized()) {
         Alert.showError(title, this.lang.get(p + "unknown"), (Throwable)null);
      } else {
         Iterator var6 = crash.getSignatures().iterator();

         while(var6.hasNext()) {
            CrashSignature sign = (CrashSignature)var6.next();
            String path = sign.path;
            String message = this.lang.get(p + path);
            String url = this.lang.get(p + path + ".url");
            URI uri = U.makeURI(url);
            if (uri != null) {
               if (Alert.showQuestion(title, message, report, false)) {
                  OperatingSystem.openLink(uri);
               }
            } else {
               Alert.showMessage(title, message, report);
            }
         }
      }

      if (report != null) {
         if (Alert.showQuestion(p + "store", false)) {
            U.log("Removing crash report...");
            File file = new File(report);
            if (!file.exists()) {
               U.log("File is already removed. LOL.");
            } else {
               try {
                  if (!file.delete()) {
                     throw new Exception("file.delete() returned false");
                  }
               } catch (Exception var11) {
                  U.log("Can't delete crash report file. Okay.");
                  Alert.showAsyncMessage(p + "store.failed", var11);
                  return;
               }

               U.log("Yay, crash report file doesn't exist by now.");
            }

            Alert.showAsyncMessage(p + "store.success");
         }

      }
   }

   public void onAuthPassing(Authenticator auth) {
      this.block("auth");
   }

   public void onAuthPassingError(Authenticator auth, Throwable e) {
      this.unblock("auth");
   }

   public void onAuthPassed(Authenticator auth) {
      this.unblock("auth");
   }

   public void onUpdateError(Update u, Throwable e) {
      this.unblock("update");
   }

   public void onUpdateDownloading(Update u) {
      this.block("update");
   }

   public void onUpdateDownloadError(Update u, Throwable e) {
      this.unblock("update");
   }

   public void onUpdateReady(Update u) {
   }

   public void onUpdateApplying(Update u) {
   }

   public void onUpdateApplyError(Update u, Throwable e) {
   }

   public void onUpdaterRequesting(Updater u) {
   }

   public void onUpdaterRequestError(Updater u) {
   }

   public void onUpdateFound(Update upd) {
      if (Updater.isAutomode()) {
         upd.addListener(this);
         this.block("update");
      }
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onAdFound(Updater u, Ad ad) {
   }
}
