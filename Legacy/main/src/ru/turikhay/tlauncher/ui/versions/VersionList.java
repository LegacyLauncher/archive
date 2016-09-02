package ru.turikhay.tlauncher.ui.versions;

import net.minecraft.launcher.updater.VersionSyncInfo;
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
import ru.turikhay.util.U;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

public class VersionList extends CenterPanel implements VersionHandlerListener {
    private static final long serialVersionUID = -7192156096621636270L;
    final VersionHandler handler;
    public final SimpleListModel<VersionSyncInfo> model;
    public final JList<VersionSyncInfo> list;
    VersionDownloadButton download;
    VersionRemoveButton remove;
    public final ImageButton refresh;
    public final ImageButton back;

    VersionList(VersionHandler h) {
        super(squareInsets);
        handler = h;
        BorderPanel panel = new BorderPanel(0, SwingUtil.magnify(5));
        LocalizableLabel label = new LocalizableLabel("version.manager.list");
        panel.setNorth(label);
        model = new SimpleListModel();
        list = new JList(model);
        list.setCellRenderer(new VersionListCellRenderer(this));
        list.setSelectionMode(2);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                handler.onVersionSelected(U.asListOf(VersionSyncInfo.class, list.getSelectedValues()));
            }
        });
        panel.setCenter(new ScrollPane(list, ScrollPane.ScrollBarPolicy.AS_NEEDED, ScrollPane.ScrollBarPolicy.NEVER));
        ExtendedPanel buttons = new ExtendedPanel(new GridLayout(0, 4));
        refresh = new VersionRefreshButton(this);
        buttons.add(refresh);
        download = new VersionDownloadButton(this);
        buttons.add(download);
        remove = new VersionRemoveButton(this);
        buttons.add(remove);
        back = new ImageButton("home.png");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.exitEditor();
            }
        });
        buttons.add(back);
        panel.setSouth(buttons);
        add(panel);
        handler.addListener(this);
        setSize(SwingUtil.magnify(new Dimension(300, 350)));
    }

    void select(List<VersionSyncInfo> list) {
        if (list != null) {
            int size = list.size();
            int[] indexes = new int[list.size()];

            for (int i = 0; i < size; ++i) {
                indexes[i] = model.indexOf(list.get(i));
            }

            this.list.setSelectedIndices(indexes);
        }
    }

    void deselect() {
        list.clearSelection();
    }

    void refreshFrom(VersionManager manager) {
        setRefresh(false);
        List list = manager.getVersions(handler.filter, false);
        model.addAll(list);
    }

    void setRefresh(boolean refresh) {
        model.clear();
        if (refresh) {
            model.add(VersionCellRenderer.LOADING);
        }

    }

    public void block(Object reason) {
        Blocker.blockComponents(reason, list, refresh, remove);
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(reason, list, refresh, remove);
    }

    public void onVersionRefreshing(VersionManager vm) {
        setRefresh(true);
    }

    public void onVersionRefreshed(VersionManager vm) {
        refreshFrom(vm);
    }

    public void onVersionSelected(List<VersionSyncInfo> version) {
    }

    public void onVersionDeselected() {
    }

    public void onVersionDownload(List<VersionSyncInfo> list) {
        select(list);
    }
}