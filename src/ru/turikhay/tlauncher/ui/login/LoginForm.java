package ru.turikhay.tlauncher.ui.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.DownloaderListener;
import ru.turikhay.tlauncher.managers.ElyManager;
import ru.turikhay.tlauncher.managers.ElyManagerListener;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Account;
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
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class LoginForm extends CenterPanel implements DownloaderListener, ElyManagerListener, VersionManagerListener, AuthenticatorListener, MinecraftListener {
   private final List stateListeners = Collections.synchronizedList(new ArrayList());
   private final List processListeners = Collections.synchronizedList(new ArrayList());
   public final DefaultScene scene;
   public final MainPane pane;
   private final SettingsPanel settings;
   public final AccountComboBox accounts;
   public final VersionComboBox versions;
   public final CheckBoxPanel checkbox;
   public final ButtonPanel buttons;
   public final AutoLogin autologin;
   private final LoginForm.StartThread startThread;
   private final LoginForm.StopThread stopThread;
   private LoginForm.LoginState state;
   private ServerList.Server server;

   public LoginForm(DefaultScene scene) {
      this.state = LoginForm.LoginState.STOPPED;
      this.scene = scene;
      this.pane = scene.getMainPane();
      this.settings = scene.settingsForm;
      this.startThread = new LoginForm.StartThread();
      this.stopThread = new LoginForm.StopThread();
      this.autologin = new AutoLogin(this);
      this.accounts = new AccountComboBox(this);
      this.buttons = new ButtonPanel(this);
      this.versions = new VersionComboBox(this);
      this.checkbox = new CheckBoxPanel(this);
      this.processListeners.add(this.autologin);
      this.processListeners.add(this.settings);
      this.processListeners.add(this.checkbox);
      this.processListeners.add(this.versions);
      this.processListeners.add(this.accounts);
      this.stateListeners.add(this.buttons.play);
      this.add(this.messagePanel);
      this.add(this.del(0));
      this.add(this.accounts);
      this.add(this.versions);
      this.add(this.del(0));
      this.add(this.checkbox);
      this.add(this.del(0));
      this.add(this.buttons);
      this.tlauncher.getElyManager().addListener(this);
      this.tlauncher.getVersionManager().addListener(this);
      this.tlauncher.getDownloader().addListener(this);
   }

   private void runProcess() {
      LoginException error = null;
      boolean success = true;
      List force = this.processListeners;
      synchronized(this.processListeners) {
         Iterator var5 = this.processListeners.iterator();

         LoginForm.LoginProcessListener listener;
         while(var5.hasNext()) {
            listener = (LoginForm.LoginProcessListener)var5.next();

            try {
               listener.logginingIn();
            } catch (LoginWaitException var12) {
               LoginWaitException loginError = var12;
               this.log(new Object[]{"Catched a wait task from listener", listener});

               try {
                  loginError.getWaitTask().runTask();
               } catch (LoginException var11) {
                  this.log(new Object[]{"Catched an error on a wait task."});
                  error = var11;
               }
            } catch (LoginException var13) {
               this.log(new Object[]{"Catched an error on a listener", listener});
               error = var13;
            }

            if (error != null) {
               this.log(new Object[]{error});
               success = false;
               break;
            }
         }

         if (success) {
            var5 = this.processListeners.iterator();

            while(var5.hasNext()) {
               listener = (LoginForm.LoginProcessListener)var5.next();
               listener.loginSucceed();
            }
         } else {
            var5 = this.processListeners.iterator();

            while(var5.hasNext()) {
               listener = (LoginForm.LoginProcessListener)var5.next();
               listener.loginFailed();
            }
         }
      }

      if (error != null) {
         this.log(new Object[]{"Login process has ended with an error."});
      } else {
         this.global.setForcefully("login.account", this.accounts.getAccount().getUsername(), false);
         this.global.setForcefully("login.account.type", this.accounts.getAccount().getType(), false);
         this.global.setForcefully("login.version", this.versions.getVersion().getID(), false);
         this.global.store();
         boolean force1 = this.checkbox.forceupdate.isSelected();
         this.changeState(LoginForm.LoginState.LAUNCHING);
         this.log(new Object[]{"Calling Minecraft Launcher..."});
         this.tlauncher.launch(this, this.server, force1);
         this.checkbox.forceupdate.setSelected(false);
      }

   }

   private void stopProcess() {
      while(!this.tlauncher.isLauncherWorking()) {
         U.sleepFor(500L);
      }

      this.changeState(LoginForm.LoginState.STOPPING);
      this.tlauncher.getLauncher().stop();
   }

   public void startLauncher() {
      this.startLauncher((ServerList.Server)null);
   }

   public void startLauncher(ServerList.Server server) {
      if (!Blocker.isBlocked(this)) {
         while(true) {
            if (this.accounts.getAccount() == null || this.accounts.getAccount().getType() != Account.AccountType.ELY || !this.tlauncher.getElyManager().isRefreshing()) {
               this.server = server;
               this.autologin.setActive(false);
               this.startThread.iterate();
               break;
            }

            U.sleepFor(500L);
         }
      }

   }

   public void stopLauncher() {
      this.stopThread.iterate();
   }

   private void changeState(LoginForm.LoginState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         if (this.state != state) {
            this.state = state;
            Iterator var3 = this.stateListeners.iterator();

            while(var3.hasNext()) {
               LoginForm.LoginStateListener listener = (LoginForm.LoginStateListener)var3.next();
               listener.loginStateChanged(state);
            }
         }

      }
   }

   public void block(Object reason) {
      if (!Blocker.getBlockList(this).contains("refresh")) {
         Blocker.block((Blockable)this.accounts, (Object)reason);
      }

      Blocker.block(reason, this.settings, this.versions, this.checkbox, this.buttons);
   }

   public synchronized void unblock(Object reason) {
      Blocker.unblock(reason, this.settings, this.accounts, this.versions, this.checkbox, this.buttons);
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

   public void onVersionsRefreshing(VersionManager manager) {
      Blocker.block((Blockable)this, (Object)"refresh");
   }

   public void onVersionsRefreshingFailed(VersionManager manager) {
      Blocker.unblock((Blockable)this, (Object)"refresh");
   }

   public void onVersionsRefreshed(VersionManager manager) {
      Blocker.unblock((Blockable)this, (Object)"refresh");
   }

   public void onElyUpdating(ElyManager manager) {
      if (this.accounts.getAccount() != null && this.accounts.getAccount().getType() == Account.AccountType.ELY) {
         Blocker.block((Blockable)this.buttons.play, (Object)"ely");
      }

      this.accounts.updateAccount();
      this.repaint();
   }

   public void onElyUpdated(ElyManager manager) {
      Blocker.unblock((Blockable)this.buttons.play, (Object)"ely");
      this.repaint();
   }

   public void onAuthPassing(Authenticator auth) {
      Blocker.block((Blockable)this, (Object)"auth");
   }

   public void onAuthPassingError(Authenticator auth, Throwable e) {
      Blocker.unblock((Blockable)this, (Object)"auth");
      if (!(e.getCause() instanceof IOException) && !(e.getCause() instanceof RuntimeException)) {
         throw new LoginException("Cannot auth!");
      }
   }

   public void onAuthPassed(Authenticator auth) {
      Blocker.unblock((Blockable)this, (Object)"auth");
   }

   public void onMinecraftPrepare() {
      Blocker.block((Blockable)this, (Object)"launch");
   }

   public void onMinecraftAbort() {
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.buttons.play.updateState();
   }

   public void onMinecraftLaunch() {
      this.changeState(LoginForm.LoginState.LAUNCHED);
   }

   public void onMinecraftClose() {
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.changeState(LoginForm.LoginState.STOPPED);
      if (this.autologin.isEnabled()) {
         this.tlauncher.getVersionManager().asyncRefresh();
         this.tlauncher.getUpdater().asyncFindUpdate();
      } else {
         this.tlauncher.getVersionManager().asyncRefresh(true);
      }

   }

   public void onMinecraftError(Throwable e) {
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.changeState(LoginForm.LoginState.STOPPED);
   }

   public void onMinecraftKnownError(MinecraftException e) {
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.changeState(LoginForm.LoginState.STOPPED);
   }

   public void onMinecraftCrash(Crash crash) {
      Blocker.unblock((Blockable)this, (Object)"launch");
      this.changeState(LoginForm.LoginState.STOPPED);
   }

   class StopThread extends LoopedThread {
      StopThread() {
         this.startAndWait();
      }

      protected void iterateOnce() {
         try {
            LoginForm.this.stopProcess();
         } catch (Throwable var2) {
            Alert.showError(var2);
         }

      }
   }

   class StartThread extends LoopedThread {
      StartThread() {
         this.startAndWait();
      }

      protected void iterateOnce() {
         try {
            LoginForm.this.runProcess();
         } catch (Throwable var2) {
            Alert.showError(var2);
         }

      }
   }

   public interface LoginStateListener {
      void loginStateChanged(LoginForm.LoginState var1);
   }

   public static enum LoginState {
      LAUNCHING,
      STOPPING,
      STOPPED,
      LAUNCHED;
   }

   public interface LoginProcessListener {
      void logginingIn() throws LoginException;

      void loginFailed();

      void loginSucceed();
   }
}
