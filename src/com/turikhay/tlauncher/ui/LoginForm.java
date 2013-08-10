package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.events.RefreshedVersionsListener;
import net.minecraft.launcher_.updater.VersionFilter;
import net.minecraft.launcher_.updater.VersionManager;
import net.minecraft.launcher_.updater.VersionSyncInfo;
import net.minecraft.launcher_.versions.Version;

public class LoginForm extends CenterPanel implements RefreshedVersionsListener, MinecraftLauncherListener {
   private static final long serialVersionUID = 6768252827144456302L;
   final LoginForm instance = this;
   final VersionManager vm;
   Panel error;
   Panel maininput;
   Panel versionchoice;
   Panel autologin;
   Panel forceupdate;
   Panel enter;
   TextField username_i;
   boolean username_i_edit;
   String username;
   Choice version_dm;
   boolean version_i;
   String version;
   Checkbox forceupdate_f;
   boolean forceupdate_e;
   Checkbox console_f;
   boolean console;
   private boolean login_blocked;
   Button login_b;
   Button settings_b;
   Button cancelautologin_b;
   boolean settings_b_pressed;

   LoginForm(TLauncherFrame fd) {
      super(fd);
      this.vm = this.t.vm;
      this.username = this.s.get("login.username");
      this.version = this.s.get("login.version");
      this.console = this.s.getBoolean("login.debug");
      this.maininput = new Panel(this.g_single);
      this.username_i_edit = this.username != null;
      this.username_i = new TextField(this.username_i_edit ? this.username : this.l.get("username"), 20);
      this.username_i.setFont(this.username_i_edit ? this.font : this.font_italic);
      this.username_i.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent arg0) {
            LoginForm.this.editUsername();
         }

         public void mouseEntered(MouseEvent arg0) {
         }

         public void mouseExited(MouseEvent arg0) {
         }

         public void mousePressed(MouseEvent arg0) {
         }

         public void mouseReleased(MouseEvent arg0) {
         }
      });
      this.username_i.addKeyListener(new KeyListener() {
         public void keyPressed(KeyEvent e) {
         }

         public void keyReleased(KeyEvent e) {
            LoginForm.this.editUsername();
            LoginForm.this.checkUsername();
         }

         public void keyTyped(KeyEvent e) {
         }
      });
      this.username_i.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            LoginForm.this.callLogin();
         }
      });
      if (this.username_i_edit) {
         this.checkUsername();
      }

      this.maininput.add(this.username_i);
      this.versionchoice = new Panel(this.g_single);
      this.version_dm = new Choice();
      this.version_dm.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            LoginForm.this.version = e.getItem().toString();
            LoginForm.this.onVersionChanged();
         }
      });
      this.versionchoice.add(this.version_dm);
      this.autologin = new Panel(this.g_zero);
      this.console_f = new Checkbox(this.l.get("debug-console"));
      this.console_f.setState(this.console);
      this.console_f.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
            case 1:
               LoginForm.this.console = true;
               break;
            case 2:
               LoginForm.this.console = false;
            }

         }
      });
      this.autologin.add(this.console_f);
      this.forceupdate_f = new Checkbox(this.l.get("forceupdate"));
      this.forceupdate_f.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
            case 1:
               LoginForm.this.forceupdate_e = true;
               break;
            case 2:
               LoginForm.this.forceupdate_e = false;
            }

         }
      });
      this.autologin.add(this.forceupdate_f);
      this.enter = new Panel(this.g_save);
      this.login_b = new Button(this.l.get("enter"));
      this.login_b.setFont(this.font_bold);
      this.login_b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            LoginForm.this.callLogin();
         }
      });
      this.settings_b = new Button() {
         private static final long serialVersionUID = 8147812298929643824L;

         public void update(Graphics g) {
            this.paint(g);
         }

         public void paint(Graphics g) {
            int offset = LoginForm.this.settings_b_pressed ? 1 : 0;
            Image img = LoginForm.this.instance.f.settings;
            int width = img.getWidth((ImageObserver)null);
            int height = img.getHeight((ImageObserver)null);
            int x = this.getWidth() / 2 - width / 2;
            int y = this.getHeight() / 2 - height / 2;
            g.drawImage(img, x + offset, y + offset, (ImageObserver)null);
            LoginForm.this.settings_b_pressed = false;
         }
      };
      this.settings_b.setPreferredSize(new Dimension(30, this.settings_b.getHeight()));
      this.settings_b.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
         }

         public void mouseEntered(MouseEvent e) {
         }

         public void mouseExited(MouseEvent e) {
         }

         public void mousePressed(MouseEvent e) {
            LoginForm.this.settings_b_pressed = true;
         }

         public void mouseReleased(MouseEvent e) {
         }
      });
      this.settings_b.addKeyListener(new KeyListener() {
         public void keyPressed(KeyEvent e) {
            if (e.getExtendedKeyCode() == 32) {
               LoginForm.this.settings_b_pressed = true;
            }
         }

         public void keyReleased(KeyEvent e) {
            LoginForm.this.settings_b_pressed = false;
         }

         public void keyTyped(KeyEvent e) {
         }
      });
      this.settings_b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            LoginForm.this.callSettings();
         }
      });
      this.enter.add("Center", this.login_b);
      this.enter.add("East", this.settings_b);
      this.add(this.error_l);
      this.add(this.maininput);
      this.add(this.versionchoice);
      this.add(this.del(1));
      this.add(this.autologin);
      this.add(this.del(-1));
      this.add(this.enter);
   }

   private void save() {
      U.log("save");
      this.s.set("login.username", this.username);
      this.s.set("login.version", this.version);
      this.s.set("login.debug", this.console);
   }

   private void editUsername() {
      if (!this.username_i_edit) {
         this.username_i.setText("");
         this.username_i.setFont(this.font);
         this.username_i_edit = true;
      }
   }

   private boolean checkUsername(boolean notEmpty) {
      String text = this.username_i.getText();
      String regexp = "^[A-Za-z0-9_-]" + (notEmpty ? "+" : "*") + "$";
      if (text.matches(regexp)) {
         this.usernameOK();
         this.username = text;
         return true;
      } else {
         if (!this.username_i.hasFocus() && this.username != null) {
            this.username_i.requestFocusInWindow();
         }

         this.usernameWrong(this.l.get("username.incorrect"));
         return false;
      }
   }

   private boolean checkUsername() {
      return this.checkUsername(false);
   }

   private void usernameWrong(String reason) {
      this.username_i.setBackground(Color.pink);
      this.setError(reason);
   }

   private void usernameOK() {
      this.username_i.setBackground(Color.white);
      this.setError((String)null);
   }

   private void onVersionChanged() {
      VersionSyncInfo selected = this.vm.getVersionSyncInfo(this.version);
      String path = selected.isInstalled() ? "enter" : "enter.install";
      this.login_b.setLabel(this.l.get(path));
      this.version_dm.setEnabled(true);
   }

   public void callLogin() {
      this.defocus();
      if (!this.login_blocked) {
         U.log("login");
         if (this.checkUsername(true)) {
            if (!this.version_i) {
               this.blockLogin();
               this.vm.asyncRefresh();
               this.unblockLogin();
               if (!this.version_i) {
                  this.setError(this.l.get("versions.notfound"));
               }

            } else {
               this.setError((String)null);
               this.save();
               this.t.launch(this, this.username, this.version, this.forceupdate_e, this.console);
            }
         }
      }
   }

   public void cancelLogin() {
      this.defocus();
      U.log("cancellogin");
      this.unblock();
   }

   public void callSettings() {
      this.defocus();
      U.log("settings");
      AsyncThread.execute(new Runnable() {
         public void run() {
            if (!OperatingSystem.openFile(MinecraftUtil.getWorkingDirectory())) {
               Alert.showError(LoginForm.this.l.get("settings.error.folder.title"), LoginForm.this.l.get("settings.error.folder", "d", MinecraftUtil.getWorkingDirectory()));
            }

         }
      });
   }

   void callAutoLogin() {
   }

   void cancelAutoLogin() {
   }

   void removeAutoLogin() {
      this.cancelautologin_b.setVisible(false);
   }

   void setAutologinRemaining(int s) {
      this.cancelautologin_b.setLabel(this.l.get("autologin.cancel", "i", s));
   }

   public void setError(String message) {
      if (message == null) {
         this.border = this.green;
         this.repaint();
         this.error_l.setText("");
      } else {
         this.border = this.red;
         this.repaint();
         this.error_l.setText(message);
      }
   }

   protected void block() {
      this.username_i.setEnabled(false);
      this.version_dm.setEnabled(false);
      this.blockLogin();
   }

   protected void unblock() {
      this.username_i.setEnabled(true);
      this.version_dm.setEnabled(true);
      this.unblockLogin();
   }

   void blockLogin() {
      this.login_blocked = true;
      this.login_b.setEnabled(false);
   }

   void unblockLogin() {
      this.login_blocked = false;
      this.login_b.setEnabled(true);
   }

   public void onVersionsRefreshed(VersionManager vm) {
      this.refreshVersions(vm, false);
   }

   public void onVersionsRefreshingFailed(VersionManager vm) {
      this.refreshVersions(vm, true);
   }

   public void onVersionsRefreshing(VersionManager vm) {
      this.version_dm.setEnabled(false);
      this.version_dm.removeAll();
      this.version_dm.add(this.l.get("versions.loading"));
      this.blockLogin();
   }

   private void refreshVersions(VersionManager vm, boolean local) {
      this.unblockLogin();
      this.version_dm.removeAll();
      VersionFilter vf = MinecraftUtil.getVersionFilter();
      List listver = local ? vm.getInstalledVersions(vf) : vm.getVersions(vf);
      Iterator var6 = listver.iterator();

      while(var6.hasNext()) {
         VersionSyncInfo curv = (VersionSyncInfo)var6.next();
         Version ver = curv.getLatestVersion();
         String toadd = ver.getId();
         this.version_dm.add(toadd);
         if (toadd.equals(this.version)) {
            this.version_dm.select(toadd);
         }
      }

      if (this.version_dm.getItemCount() != 0) {
         if (this.version == null) {
            this.version = this.version_dm.getItem(0);
         }

         this.onVersionChanged();
         this.version_i = true;
      } else {
         this.version_dm.add(this.l.get("versions.notfound.tip"));
      }
   }

   public void onMinecraftCheck() {
      this.block();
   }

   public void onMinecraftPrepare() {
      this.block();
   }

   public void onMinecraftLaunch() {
      this.unblock();
      this.f.mc.sun.suspend();
      this.vm.asyncRefresh();
   }

   public void onMinecraftClose() {
      this.unblock();
      this.f.mc.sun.resume();
   }

   public void onMinecraftError(Throwable e) {
      this.unblock();
      Alert.showError(this.l.get("launcher.error.title"), this.l.get("launcher.error.unknown"), e);
   }

   public void onMinecraftError(String message) {
      this.unblock();
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(message));
   }

   public void onMinecraftError(MinecraftLauncherException knownError) {
      this.unblock();
      Alert.showError(this.l.get("launcher.error.title"), this.l.get(knownError.getLangpath(), "r", knownError.getReplace()));
   }

   public void onMinecraftWarning(String message, String replace) {
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
      this.block();
   }

   public void onUpdaterDownloadError(Updater u, Throwable e) {
   }

   public void onUpdaterProcessError(Updater u, Throwable e) {
      this.unblock();
   }
}
