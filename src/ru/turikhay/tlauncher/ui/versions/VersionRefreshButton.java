package ru.turikhay.tlauncher.ui.versions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JPopupMenu;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.ImageButton;

public class VersionRefreshButton extends ImageButton implements Blockable, VersionHandlerListener {
   final VersionHandler handler;
   private final JPopupMenu menu;
   private final LocalizableMenuItem local;
   private final LocalizableMenuItem remote;
   private VersionRefreshButton.ButtonState state;

   VersionRefreshButton(VersionList list) {
      this.handler = list.handler;
      this.menu = new JPopupMenu();
      this.local = new LocalizableMenuItem("version.manager.refresher.menu.local");
      this.local.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionRefreshButton.this.handler.refresh();
         }
      });
      this.menu.add(this.local);
      this.remote = new LocalizableMenuItem("version.manager.refresher.menu.remote");
      this.remote.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionRefreshButton.this.handler.asyncRefresh();
         }
      });
      this.menu.add(this.remote);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionRefreshButton.this.onPressed();
         }
      });
      this.setState(VersionRefreshButton.ButtonState.REFRESH);
      this.handler.addListener(this);
   }

   void onPressed() {
      switch(this.state) {
      case REFRESH:
         this.menu.show(this, 0, this.getHeight());
         break;
      case CANCEL:
         this.handler.stopRefresh();
      }

   }

   private void setState(VersionRefreshButton.ButtonState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         this.setImage(state.image);
      }
   }

   public void onVersionRefreshing(VersionManager vm) {
      this.setState(VersionRefreshButton.ButtonState.CANCEL);
   }

   public void onVersionRefreshed(VersionManager vm) {
      this.setState(VersionRefreshButton.ButtonState.REFRESH);
   }

   public void onVersionSelected(List versions) {
   }

   public void onVersionDeselected() {
   }

   public void onVersionDownload(List list) {
   }

   public void block(Object reason) {
      if (!reason.equals("refresh")) {
         this.setEnabled(false);
      }

   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }

   static enum ButtonState {
      REFRESH("refresh.png"),
      CANCEL("cancel.png");

      final Image image;

      private ButtonState(String image) {
         this.image = Images.getImage(image);
      }
   }
}
