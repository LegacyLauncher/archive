package ru.turikhay.tlauncher.ui.versions;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.ScrollPane.ScrollBarPolicy;
import ru.turikhay.tlauncher.ui.swing.SimpleListModel;
import ru.turikhay.tlauncher.ui.swing.VersionCellRenderer;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class VersionList extends CenterPanel implements VersionHandlerListener {
	private static final long serialVersionUID = -7192156096621636270L;

	final VersionHandler handler;

	public final SimpleListModel<VersionSyncInfo> model;
	public final JList<VersionSyncInfo> list;

	VersionDownloadButton download;
	VersionRemoveButton remove;

	public final ImageButton refresh, back;

	VersionList(VersionHandler h) {
		super(squareInsets);

		this.handler = h;

		BorderPanel panel = new BorderPanel(0, 5);

		LocalizableLabel label = new LocalizableLabel("version.manager.list");
		panel.setNorth(label);

		this.model = new SimpleListModel<VersionSyncInfo>();
		this.list = new JList<VersionSyncInfo>(model);
		list.setCellRenderer(new VersionListCellRenderer(this));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				handler.onVersionSelected(list.getSelectedValuesList());
			}
		});
		panel.setCenter(new ScrollPane(list, ScrollBarPolicy.AS_NEEDED, ScrollBarPolicy.NEVER));

		ExtendedPanel buttons = new ExtendedPanel(new GridLayout(0, 4));

		this.refresh = new VersionRefreshButton(this);
		buttons.add(refresh);

		this.download = new VersionDownloadButton(this);
		buttons.add(download);

		this.remove = new VersionRemoveButton(this);
		buttons.add(remove);

		this.back = new ImageButton("home.png");
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handler.exitEditor();
			}
		});
		buttons.add(back);

		panel.setSouth(buttons);
		add(panel);

		handler.addListener(this);

		setSize(VersionHandler.ELEM_WIDTH, 350);
	}

	void select(List<VersionSyncInfo> list) {
		if(list == null) return;

		int size = list.size();
		int[] indexes = new int[list.size()];

		for(int i=0;i<size;i++)
			indexes[i] = model.indexOf( list.get(i) );

		this.list.setSelectedIndices(indexes);
	}

	void deselect() {
		list.clearSelection();
	}

	void refreshFrom(VersionManager manager) {
		setRefresh(false);

		List<VersionSyncInfo> list = manager.getVersions(handler.filter, false);
		model.addAll(list);
	}

	void setRefresh(boolean refresh) {
		model.clear();

		if(refresh)
			model.add(VersionCellRenderer.LOADING);
	}

	@Override
	public void block(Object reason) {
		Blocker.blockComponents(reason, list, refresh, remove);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(reason, list, refresh, remove);
	}

	@Override
	public void onVersionRefreshing(VersionManager vm) {
		setRefresh(true);
	}

	@Override
	public void onVersionRefreshed(VersionManager vm) {
		refreshFrom(vm);
	}

	@Override
	public void onVersionSelected(List<VersionSyncInfo> version) {
	}

	@Override
	public void onVersionDeselected() {
	}

	@Override
	public void onVersionDownload(List<VersionSyncInfo> list) {
		select(list);
	}

}
