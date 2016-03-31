package ru.turikhay.tlauncher.ui.versions;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.SimpleListModel;
import ru.turikhay.tlauncher.ui.swing.VersionCellRenderer;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

public class VersionList extends CenterPanel implements VersionHandlerListener {
   final VersionHandler handler;
   public final SimpleListModel model;
   public final JList list;
   VersionDownloadButton download;
   VersionRemoveButton remove;
   public final ImageButton refresh;
   public final ImageButton back;

   VersionList(VersionHandler h) {
      super(squareInsets);
      this.handler = h;
      BorderPanel panel = new BorderPanel(0, SwingUtil.magnify(5));
      LocalizableLabel label = new LocalizableLabel("version.manager.list");
      panel.setNorth(label);
      this.model = new SimpleListModel();
      this.list = new JList(this.model);
      this.list.setCellRenderer(new VersionListCellRenderer(this));
      this.list.setSelectionMode(2);
      this.list.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent e) {
            VersionList.this.handler.onVersionSelected(VersionList.this.list.getSelectedValuesList());
         }
      });
      panel.setCenter(new ScrollPane(this.list, ScrollPane.ScrollBarPolicy.AS_NEEDED, ScrollPane.ScrollBarPolicy.NEVER));
      ExtendedPanel buttons = new ExtendedPanel(new GridLayout(0, 4));
      this.refresh = new VersionRefreshButton(this);
      buttons.add(this.refresh);
      this.download = new VersionDownloadButton(this);
      buttons.add(this.download);
      this.remove = new VersionRemoveButton(this);
      buttons.add(this.remove);
      this.back = new ImageButton("home.png");
      this.back.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            VersionList.this.handler.exitEditor();
         }
      });
      buttons.add(this.back);
      panel.setSouth(buttons);
      this.add(panel);
      this.handler.addListener(this);
      this.setSize(SwingUtil.magnify(new Dimension(300, 350)));
   }

   void select(List list) {
      if (list != null) {
         int size = list.size();
         int[] indexes = new int[list.size()];

         for(int i = 0; i < size; ++i) {
            indexes[i] = this.model.indexOf(list.get(i));
         }

         this.list.setSelectedIndices(indexes);
      }

   }

   void deselect() {
      this.list.clearSelection();
   }

   void refreshFrom(VersionManager manager) {
      this.setRefresh(false);
      List list = manager.getVersions(this.handler.filter, false);
      this.model.addAll(list);
   }

   void setRefresh(boolean refresh) {
      this.model.clear();
      if (refresh) {
         this.model.add(VersionCellRenderer.LOADING);
      }

   }

   public void block(Object reason) {
      Blocker.blockComponents(reason, this.list, this.refresh, this.remove);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(reason, this.list, this.refresh, this.remove);
   }

   public void onVersionRefreshing(VersionManager vm) {
      this.setRefresh(true);
   }

   public void onVersionRefreshed(VersionManager vm) {
      this.refreshFrom(vm);
   }

   public void onVersionSelected(List version) {
   }

   public void onVersionDeselected() {
   }

   public void onVersionDownload(List list) {
      this.select(list);
   }
}
