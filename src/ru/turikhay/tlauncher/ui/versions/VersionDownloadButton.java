package ru.turikhay.tlauncher.ui.versions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.AbortedDownloadException;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionSyncInfoContainer;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.ImageButton;

public class VersionDownloadButton extends ImageButton implements VersionHandlerListener, Unblockable {
	private static final String
	SELECTION_BLOCK = "selection",

	PREFIX = "version.manager.downloader.",

	WARNING = PREFIX + "warning.",
	WARNING_TITLE = WARNING + "title",
	WARNING_FORCE = WARNING + "force.",

	ERROR = PREFIX + "error.",
	ERROR_TITLE = ERROR + "title",

	INFO = PREFIX + "info.",
	INFO_TITLE = INFO + "title",

	MENU = PREFIX + "menu.";

	final VersionHandler handler;
	final Blockable blockable;

	private final JPopupMenu menu;
	private final LocalizableMenuItem ordinary, force;

	private ButtonState state;
	private boolean downloading, aborted;

	boolean forceDownload;

	VersionDownloadButton(VersionList list) {
		this.handler = list.handler;

		this.blockable = new Blockable() {
			@Override
			public void block(Object reason) {
				setEnabled(false);
			}

			@Override
			public void unblock(Object reason) {
				setEnabled(true);
			}
		};

		this.menu = new JPopupMenu();

		this.ordinary = new LocalizableMenuItem(MENU + "ordinary");
		ordinary.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				forceDownload = false;
				onDownloadCalled();
			}
		});
		menu.add(ordinary);

		this.force = new LocalizableMenuItem(MENU + "force");
		force.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				forceDownload = true;
				onDownloadCalled();
			}
		});
		menu.add(force);

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onPressed();
			}
		});

		this.setState(ButtonState.DOWNLOAD);
		handler.addListener(this);
	}

	void setState(ButtonState state) {
		if(state == null)
			throw new NullPointerException();

		this.state = state;
		this.setImage(state.image);
	}

	void onPressed() {
		switch(state) {
		case DOWNLOAD:
			onDownloadPressed();
			break;
		case STOP:
			onStopCalled();
			break;
		}
	}

	void onDownloadPressed() {
		menu.show(this, 0, getHeight());
	}

	void onDownloadCalled() {
		if(state != ButtonState.DOWNLOAD)
			throw new IllegalStateException();

		handler.thread.startThread.iterate();
	}

	void onStopCalled() {
		if(state != ButtonState.STOP)
			throw new IllegalStateException();

		handler.thread.stopThread.iterate();
	}

	@SuppressWarnings("null")
	void startDownload() {
		this.aborted = false;
		List<VersionSyncInfo> list = handler.getSelectedList();

		if(list == null || list.isEmpty())
			return;

		int countLocal = 0;
		VersionSyncInfo local = null;

		for(VersionSyncInfo version : list) {

			if(forceDownload)
				if(!version.hasRemote()) {
					Alert.showError(Localizable.get(ERROR_TITLE), Localizable.get(ERROR + "local", version.getID()));
					return;
				}
				else if(version.isUpToDate() && version.isInstalled()) {
					countLocal++;
					local = version;
				}
		}

		if(countLocal > 0) {
			String title = Localizable.get(WARNING_TITLE);
			String suffix; Object var;

			if(countLocal == 1) {
				suffix = "single";
				var = local.getID();
			} else {
				suffix = "multiply";
				var = countLocal;
			}

			if(!Alert.showQuestion(title, Localizable.get(WARNING_FORCE + suffix, var)))
				return;
		}

		List<VersionSyncInfoContainer> containers = new ArrayList<VersionSyncInfoContainer>();
		final VersionManager manager = TLauncher.getInstance().getVersionManager();

		try {
			downloading = true;

			for(VersionSyncInfo version : list) {
				try {
					version.resolveCompleteVersion(manager, forceDownload);
					VersionSyncInfoContainer container = manager.downloadVersion(version, forceDownload);

					if(aborted)
						return;

					if(!container.getList().isEmpty())
						containers.add(container);

				} catch (Exception e) {
					Alert.showError(Localizable.get(ERROR_TITLE), Localizable.get(ERROR + "getting", version.getID()), e);
					return;
				}
			}

			if(containers.isEmpty()) {
				Alert.showMessage(Localizable.get(INFO_TITLE), Localizable.get(INFO + "no-needed"));
				return;
			}

			if(containers.size() > 1)
				DownloadableContainer.removeDublicates(containers);

			if(aborted)
				return;

			for(DownloadableContainer c : containers)
				handler.downloader.add(c);

			handler.downloading = list;
			handler.onVersionDownload(list);

			handler.downloader.startDownloadAndWait();

		} finally {
			downloading = false;
		}

		handler.downloading.clear();

		for(VersionSyncInfoContainer container : containers) {
			List<Throwable> errors = container.getErrors();
			VersionSyncInfo version = container.getVersion();


			if(errors.isEmpty())
				try {
					manager.getLocalList().saveVersion(version.getCompleteVersion(forceDownload));
				} catch (IOException e) {
					Alert.showError(Localizable.get(ERROR_TITLE), Localizable.get(ERROR + "saving", version.getID()), e);
					return;
				}
			else
				if(!(errors.get(0) instanceof AbortedDownloadException))
					Alert.showError(Localizable.get(ERROR_TITLE), Localizable.get(ERROR + "downloading", version.getID()), errors);
		}

		handler.refresh();
	}

	void stopDownload() {
		aborted = true;

		if(downloading)
			handler.downloader.stopDownloadAndWait();
	}

	public enum ButtonState {
		DOWNLOAD("down.png"), STOP("cancel.png");

		final Image image;

		ButtonState(String image) {
			this.image = ImageCache.getImage(image);
		}
	}

	@Override
	public void onVersionRefreshing(VersionManager vm) {
	}

	@Override
	public void onVersionRefreshed(VersionManager vm) {
	}

	@Override
	public void onVersionSelected(List<VersionSyncInfo> versions) {
		if(!downloading)
			blockable.unblock(SELECTION_BLOCK);
	}

	@Override
	public void onVersionDeselected() {
		if(!downloading)
			blockable.block(SELECTION_BLOCK);
	}

	@Override
	public void onVersionDownload(List<VersionSyncInfo> list) {
	}
}
