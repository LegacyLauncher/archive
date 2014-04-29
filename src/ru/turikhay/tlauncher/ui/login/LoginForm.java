package ru.turikhay.tlauncher.ui.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.DownloaderListener;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Authenticator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.util.async.LoopedThread;

public class LoginForm extends CenterPanel implements MinecraftListener, AuthenticatorListener, VersionManagerListener, DownloaderListener {
   private static final long serialVersionUID = -7539492515708852727L;
   public static final String LOGIN_BLOCK = "login";
   public static final String REFRESH_BLOCK = "refresh";
   public static final String LAUNCH_BLOCK = "launch";
   public static final String AUTH_BLOCK = "auth";
   public static final String UPDATER_BLOCK = "update";
   public static final String DOWNLOADER_BLOCK = "download";
   private final List listeners = new ArrayList();
   private final LoginForm.LoginThread thread;
   public final DefaultScene scene;
   private final LoginForm instance = this;
   private final SettingsPanel settings;
   public final MainPane pane;
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
      TLauncher launcher = TLauncher.getInstance();
      launcher.getVersionManager().addListener(this);
      launcher.getDownloader().addListener(this);
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

   public void onDownloaderStart(Downloader d, int files) {
      Blocker.block((Blockable)this, (Object)"download");
   }

   public void onDownloaderAbort(Downloader d) {
      Blocker.unblock((Blockable)this, (Object)"download");
   }

   public void onDownloaderProgress(Downloader d, double progress, double speed) {
   }

   public void onDownloaderFileComplete(Downloader d, Downloadable file) {
   }

   public void onDownloaderComplete(Downloader d) {
      Blocker.unblock((Blockable)this, (Object)"download");
   }

   class LoginThread extends LoopedThread {
      private final LoginForm loginForm;

      LoginThread(LoginForm loginForm) {
         super("LoginThread");
         this.loginForm = loginForm;
         super.startAndWait();
      }

      public void start() {
         this.unblockThread("iteration");
      }

      protected void iterateOnce() {
         try {
            this.loginForm.runLogin();
         } catch (Throwable var2) {
            Alert.showError(var2);
         }

      }
   }
}
