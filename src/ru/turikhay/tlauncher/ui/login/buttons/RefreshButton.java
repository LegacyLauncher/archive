package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.tlauncher.managers.ComponentManagerListener;
import ru.turikhay.tlauncher.managers.ComponentManagerListenerHelper;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.updater.AdParser;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class RefreshButton extends ImageButton implements Blockable, ComponentManagerListener, UpdaterListener {
   private static final long serialVersionUID = -1334187593288746348L;
   private static final int TYPE_REFRESH = 0;
   private static final int TYPE_CANCEL = 1;
   private LoginForm lf;
   private int type;
   private final Image refresh;
   private final Image cancel;
   private Updater updaterFlag;

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
   }

   RefreshButton(LoginForm loginform) {
      this(loginform, 0);
   }

   private void onPressButton() {
      switch(this.type) {
      case 0:
         if (this.updaterFlag != null) {
            this.updaterFlag.asyncFindUpdate();
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
   }

   public void onUpdaterRequestError(Updater u) {
      this.updaterFlag = u;
   }

   public void onUpdateFound(Update upd) {
      this.updaterFlag = null;
   }

   public void onUpdaterNotFoundUpdate(Updater u) {
      this.updaterFlag = null;
   }

   public void onAdFound(Updater u, AdParser ad) {
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
}
