package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.Crash;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.settings.SettingsPanel;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.util.U;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher.events.RefreshedListener;
import net.minecraft.launcher.updater.VersionManager;

public class LoginForm extends CenterPanel implements MinecraftLauncherListener, AuthenticatorListener, RefreshedListener, UpdaterListener, UpdateListener {
   private static final long serialVersionUID = -7539492515708852727L;
   public static final String LOGIN_BLOCK = "login";
   public static final String REFRESH_BLOCK = "refresh";
   public static final String LAUNCH_BLOCK = "launch";
   public static final String AUTH_BLOCK = "auth";
   public static final String UPDATER_BLOCK = "update";
   final List listeners = new ArrayList();
   final LoginForm.LoginThread thread;
   public final DefaultScene scene;
   final LoginForm instance = this;
   final SettingsPanel settings;
   final MainPane pane;
   public final AccountComboBox accounts;
   public final VersionComboBox versions;
   public final CheckBoxPanel checkbox;
   public final ButtonPanel buttons;
   public final AutoLogin autologin;

   public LoginForm(DefaultScene scene) {
      this.scene = scene;
      this.settings = scene.settingsForm;
      this.pane = scene.getMainPane();
      this.thread = new LoginForm.LoginThread(this);
      this.autologin = new AutoLogin(this);
      this.accounts = new AccountComboBox(this);
      this.versions = new VersionComboBox(this);
      this.checkbox = new CheckBoxPanel(this);
      this.buttons = new ButtonPanel(this);
      this.listeners.add(this.autologin);
      this.listeners.add(this.checkbox);
      this.listeners.add(this.versions);
      this.listeners.add(this.accounts);
      this.add(this.messagePanel);
      this.add(this.del(0));
      this.add(this.accounts);
      this.add(this.versions);
      this.add(this.del(0));
      this.add(this.checkbox);
      this.add(this.del(0));
      this.add(this.buttons);
      TLauncher.getInstance().getVersionManager().addRefreshedListener(this);
   }

   private void saveValues() {
      this.log(new Object[]{"Saving values..."});
      this.global.set("login.account", this.accounts.getAccount().getUsername(), false);
      this.global.set("login.version", this.versions.getVersion().getId(), false);
      this.global.store();
      this.log(new Object[]{"Values has been saved!"});
   }

   public void callLogin() {
      if (Blocker.isBlocked(this)) {
         this.log(new Object[]{"Cannot call login, UI is blocked by:", Blocker.getBlockList(this)});
      } else {
         this.autologin.setActive(false);
         this.thread.start();
      }
   }

   private void runLogin() {
      this.log(new Object[]{"Running login process from a thread"});
      LoginException error = null;
      boolean success = true;
      synchronized(this.listeners) {
         Iterator var5 = this.listeners.iterator();

         LoginListener listener;
         while(var5.hasNext()) {
            listener = (LoginListener)var5.next();
            this.log(new Object[]{"Running on a listener", listener.getClass().getSimpleName()});

            try {
               listener.onLogin();
            } catch (LoginWaitException var9) {
               LoginWaitException wait = var9;
               this.log(new Object[]{"Catched a wait task from this listener, waiting..."});

               try {
                  wait.getWaitTask().runTask();
               } catch (LoginException var8) {
                  this.log(new Object[]{"Catched an error on a wait task."});
                  error = var8;
               }
            } catch (LoginException var10) {
               this.log(new Object[]{"Catched an error on a listener"});
               error = var10;
            }

            if (error != null) {
               this.log(new Object[]{error});
               success = false;
               break;
            }
         }

         var5 = this.listeners.iterator();

         while(var5.hasNext()) {
            listener = (LoginListener)var5.next();
            if (success) {
               listener.onLoginSuccess();
            } else {
               listener.onLoginFailed();
            }
         }
      }

      if (error == null) {
         this.log(new Object[]{"Login process is OK :)"});
         this.saveValues();
         boolean force = this.checkbox.forceupdate.isSelected();
         this.tlauncher.launch(this.instance, force);
         this.checkbox.forceupdate.setSelected(false);
      } else {
         this.log(new Object[]{"Login process has ended with an error."});
      }
   }

   public void block(Object reason) {
      if (!reason.equals("refresh")) {
         Blocker.block((Blockable)this.accounts, (Object)reason);
      }

      Blocker.block(reason, this.settings, this.versions, this.checkbox, this.buttons);
   }

   public void unblock(Object reason) {
      Blocker.unblock(reason, this.settings, this.accounts, this.versions, this.checkbox, this.buttons);
   }

   public void onAuthPassing(Authenticator auth) {
      Blocker.block((Blockable)this, (Object)"auth");
   }

   public void onAuthPassingError(Authenticator auth, Throwable e) {
      Blocker.unblock((Blockable)this, (Object)"auth");
      Throwable cause = e.getCause();
      if (cause == null || !(e.getCause() instanceof IOException)) {
         throw new LoginException("Cannot auth!");
      }
   }

   public void onAuthPassed(Authenticator auth) {
      Blocker.unblock((Blockable)this, (Object)"auth");
   }

   public void onVersionManagerUpdated(VersionManager vm) {
   }

   public void onVersionsRefreshing(VersionManager vm) {
      Blocker.block((Blockable)this, (Object)"refresh");
   }

   public void onVersionsRefreshingFailed(VersionManager vm) {
      Blocker.unblock((Blockable)this, (Object)"refresh");
   }

   public void onVersionsRefreshed(VersionManager vm) {
      Blocker.unblock((Blockable)this, (Object)"refresh");
   }

   public void onMinecraftCheck() {
      Blocker.block((Blockable)this, (Object)"launch");
   }

   public void onMinecraftPrepare() {
   }

   public void onMinecraftLaunch() {
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.tlauncher.hide();
      this.tlauncher.getVersionManager().asyncRefresh();
      this.tlauncher.getUpdater().asyncFindUpdate();
   }

   public void onMinecraftLaunchStop() {
      Blocker.unblock((Blockable)this, (Object)"launch");
   }

   public void onMinecraftClose() {
      this.tlauncher.show();
      this.tlauncher.getUpdaterListener().applyDelayedUpdate();
   }

   public void onMinecraftKnownError(MinecraftLauncherException knownError) {
      Alert.showError(this.lang.get("launcher.error.title"), this.lang.get(knownError.getLangpath()), knownError.getReplace());
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.tlauncher.show();
   }

   public void onMinecraftError(Throwable unknownError) {
      Alert.showError(this.lang.get("launcher.error.title"), this.lang.get("launcher.error.unknown"), unknownError);
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.tlauncher.show();
   }

   public void onMinecraftWarning(String langpath, Object replace) {
      Alert.showAsyncWarning(this.lang.get("launcher.warning.title"), this.lang.get("launcher.warning." + langpath, replace));
   }

   public void onMinecraftCrash(Crash crash) {
      Crash.handle(crash);
   }

   public void onUpdateError(Update u, Throwable e) {
      Blocker.unblock((Blockable)this, (Object)"update");
   }

   public void onUpdateDownloading(Update u) {
      Blocker.block((Blockable)this, (Object)"update");
   }

   public void onUpdateDownloadError(Update u, Throwable e) {
      Blocker.unblock((Blockable)this, (Object)"update");
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
         Blocker.block((Blockable)this, (Object)"update");
      }
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
   }

   public void onAdFound(Updater u, Ad ad) {
   }

   class LoginThread extends Thread {
      private final LoginForm loginForm;
      private boolean isStarted;
      private boolean isRunning;

      LoginThread(LoginForm loginForm) {
         this.loginForm = loginForm;
      }

      public void start() {
         if (this.isRunning) {
            LoginForm.this.log(new Object[]{"LoginThread is runned before and hasn't been stopped yet."});
         } else {
            this.isRunning = true;
            if (!this.isStarted) {
               this.isStarted = true;
               super.start();
            }
         }
      }

      public void run() {
         Blocker.block((Blockable)this.loginForm, (Object)"login");

         try {
            this.loginForm.runLogin();
         } catch (Exception var2) {
            Alert.showError(var2, false);
         }

         Blocker.unblock((Blockable)this.loginForm, (Object)"login");
         this.isRunning = false;

         while(!this.isRunning) {
            U.sleepFor(100L);
         }

         this.run();
      }
   }
}
