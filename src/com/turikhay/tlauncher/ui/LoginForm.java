package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.CrashSignature;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher_.OperatingSystem;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener, AuthenticatorListener, UpdaterListener, UpdateListener {
   private static final long serialVersionUID = 6768252827144456302L;
   private final String LAUNCH_BLOCK = "launch";
   private final String AUTH_BLOCK = "auth";
   private final String UPDATER_BLOCK = "update";
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
      String username = this.s.nget("login.username");
      String version = this.s.nget("login.version");
      boolean auto = this.s.getBoolean("login.auto");
      boolean console = this.s.getBoolean("login.debug");
      int timeout = this.s.getInteger("login.auto.timeout");
      this.autologin = new Autologin(this, auto, timeout);
      this.maininput = new MainInputPanel(this, username);
      this.versionchoice = new VersionChoicePanel(this, version);
      this.checkbox = new CheckBoxPanel(this, auto, console);
      this.buttons = new ButtonPanel(this);
      this.addListener(this.autologin);
      this.addListener(this.maininput);
      this.addListener(this.settings);
      this.addListener(this.versionchoice);
      this.addListener(this.checkbox);
      this.add(this.error);
      this.add(this.maininput);
      this.add(this.versionchoice);
      this.add(this.del(1));
      this.add(this.checkbox);
      this.add(this.del(0));
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
         boolean force = this.checkbox.getForceUpdate();
         U.log("Loggining in (force update: " + force + ")...");
         if (!this.listenerOnLogin()) {
            U.log("Login cancelled");
         } else {
            this.save();
            this.t.launch(this, force);
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
         this.autologin.cancel();
      } else {
         Alert.showAsyncMessage("loginform.checkbox.autologin.tip", this.l.get("loginform.checkbox.autologin.tip.arg"));
         this.autologin.enabled = true;
      }

      this.s.set("login.auto", this.autologin.enabled);
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

   protected void blockElement(Object reason) {
      this.defocus();
      this.maininput.blockElement(reason);
      this.versionchoice.blockElement(reason);
      this.checkbox.blockElement(reason);
      this.buttons.blockElement(reason);
      this.settings.blockElement(reason);
   }

   protected void unblockElement(Object reason) {
      this.defocus();
      this.maininput.unblockElement(reason);
      this.versionchoice.unblockElement(reason);
      this.checkbox.unblockElement(reason);
      this.buttons.unblockElement(reason);
      this.settings.unblockElement(reason);
   }

   public void onMinecraftCheck() {
      this.block("launch");
   }

   public void onMinecraftPrepare() {
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
   }

   public void onMinecraftWarning(String langpath, Object replace) {
      Alert.showWarning(this.l.get("launcher.warning.title"), this.l.get("launcher.warning." + langpath, "r", replace));
   }

   public void updateLocale() {
      super.updateLocale();
      TLauncherFrame.updateContainer(this, true);
   }

   public void onMinecraftCrash(Crash crash) {
      String p = "crash.";
      String title = this.l.get(p + "title");
      String report = crash.getFile();
      if (!crash.isRecognized()) {
         Alert.showError(title, this.l.get(p + "unknown"), (Throwable)null);
      } else {
         Iterator var6 = crash.getSignatures().iterator();

         while(var6.hasNext()) {
            CrashSignature sign = (CrashSignature)var6.next();
            String path = sign.path;
            String message = this.l.get(p + path);
            String url = this.l.get(p + path + ".url");
            URI uri = U.makeURI(url);
            if (uri != null) {
               if (Alert.showQuestion(title, message, report, false)) {
                  OperatingSystem.openLink(uri);
               }
            } else {
               Alert.showMessage(title, message, report);
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
                  Alert.showAsyncMessage(p + "store.success");
               }
            }

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

   public void onUpdateFound(Updater u, Update upd) {
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
