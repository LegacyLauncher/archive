package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.managers.VersionManager;
import com.turikhay.tlauncher.managers.VersionManagerListener;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.minecraft.crash.Crash;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.settings.SettingsPanel;
import com.turikhay.util.async.ExtendedThread;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginForm extends CenterPanel implements MinecraftListener, AuthenticatorListener, VersionManagerListener {
   private static final long serialVersionUID = -7539492515708852727L;
   public static final String LOGIN_BLOCK = "login";
   public static final String REFRESH_BLOCK = "refresh";
   private static final String LAUNCH_BLOCK = "launch";
   private static final String AUTH_BLOCK = "auth";
   public static final String UPDATER_BLOCK = "update";
   private final List listeners = new ArrayList();
   private final LoginForm.LoginThread thread;
   public final DefaultScene scene;
   private final LoginForm instance = this;
   private final SettingsPanel settings;
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
      this.listeners.add(this.settings);
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
      TLauncher.getInstance().getVersionManager().addListener(this);
   }

   private void saveValues() {
      this.log(new Object[]{"Saving values..."});
      this.global.setForcefully("login.account", this.accounts.getAccount().getUsername(), false);
      this.global.setForcefully("login.version", this.versions.getVersion().getID(), false);
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

   public void onVersionsRefreshing(VersionManager vm) {
      Blocker.block((Blockable)this, (Object)"refresh");
   }

   public void onVersionsRefreshingFailed(VersionManager vm) {
      Blocker.unblock((Blockable)this, (Object)"refresh");
   }

   public void onVersionsRefreshed(VersionManager vm) {
      Blocker.unblock((Blockable)this, (Object)"refresh");
   }

   public void onMinecraftPrepare() {
      Blocker.block((Blockable)this, (Object)"launch");
   }

   public void onMinecraftAbort() {
      Blocker.unblock((Blockable)this, (Object)"launch");
   }

   public void onMinecraftLaunch() {
   }

   public void onMinecraftClose() {
      Blocker.unblock((Blockable)this, (Object)"launch");
   }

   public void onMinecraftKnownError(MinecraftException knownError) {
      Blocker.unblock((Blockable)this, (Object)"launch");
   }

   public void onMinecraftError(Throwable unknownError) {
      Blocker.unblock((Blockable)this, (Object)"launch");
   }

   public void onMinecraftCrash(Crash crash) {
      Blocker.unblock((Blockable)this, (Object)"launch");
   }

   class LoginThread extends ExtendedThread {
      private final LoginForm loginForm;

      LoginThread(LoginForm loginForm) {
         super("LoginThread");
         this.loginForm = loginForm;
         super.start();
      }

      public void start() {
         this.unblockThread("launch");
      }

      public void run() {
         while(true) {
            this.blockThread("launch");

            try {
               this.loginForm.runLogin();
            } catch (Throwable var2) {
               Alert.showError(var2);
            }
         }
      }
   }
}
