package ru.turikhay.tlauncher.ui.versions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPopupMenu;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.ImageButton;

public class VersionRemoveButton extends ImageButton implements VersionHandlerListener, Blockable {
   private static final long serialVersionUID = 427368162418879141L;
   private static final String ILLEGAL_SELECTION_BLOCK = "illegal-selection";
   private static final String PREFIX = "version.manager.delete.";
   private static final String ERROR = "version.manager.delete.error.";
   private static final String ERROR_TITLE = "version.manager.delete.error.title";
   private static final String MENU = "version.manager.delete.menu.";
   private final VersionHandler handler;
   private final JPopupMenu menu;
   private final LocalizableMenuItem onlyJar;
   private final LocalizableMenuItem withLibraries;
   private boolean libraries;

   VersionRemoveButton(VersionList list) {
      super("remove.png");
      this.handler = list.handler;
      this.handler.addListener(this);
      this.menu = new JPopupMenu();
      this.onlyJar = new LocalizableMenuItem("version.manager.delete.menu.jar");
      this.onlyJar.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionRemoveButton.this.onChosen(false);
         }
      });
      this.menu.add(this.onlyJar);
      this.withLibraries = new LocalizableMenuItem("version.manager.delete.menu.libraries");
      this.withLibraries.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionRemoveButton.this.onChosen(true);
         }
      });
      this.menu.add(this.withLibraries);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionRemoveButton.this.onPressed();
         }
      });
   }

   void onPressed() {
      this.menu.show(this, 0, this.getHeight());
   }

   void onChosen(boolean removeLibraries) {
      this.libraries = removeLibraries;
      this.handler.thread.deleteThread.iterate();
   }

   void delete() {
      if (this.handler.selected != null) {
         LocalVersionList localList = this.handler.vm.getLocalList();
         ArrayList errors = new ArrayList();
         Iterator message = this.handler.selected.iterator();

         while(message.hasNext()) {
            VersionSyncInfo title = (VersionSyncInfo)message.next();
            if (title.isInstalled()) {
               try {
                  localList.deleteVersion(title.getID(), this.libraries);
               } catch (Throwable var6) {
                  errors.add(var6);
               }
            }
         }

         if (!errors.isEmpty()) {
            String title1 = Localizable.get("version.manager.delete.error.title");
            String message1 = Localizable.get("version.manager.delete.error." + (errors.size() == 1 ? "single" : "multiply"), errors);
            Alert.showError(title1, message1);
         }
      }

      this.handler.refresh();
   }

   public void onVersionRefreshing(VersionManager vm) {
   }

   public void onVersionRefreshed(VersionManager vm) {
   }

   public void onVersionSelected(List versions) {
      boolean onlyRemote = true;
      Iterator var4 = versions.iterator();

      while(var4.hasNext()) {
         VersionSyncInfo version = (VersionSyncInfo)var4.next();
         if (version.isInstalled()) {
            onlyRemote = false;
            break;
         }
      }

      Blocker.setBlocked(this, "illegal-selection", onlyRemote);
   }

   public void onVersionDeselected() {
      Blocker.block((Blockable)this, (Object)"illegal-selection");
   }

   public void onVersionDownload(List list) {
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
