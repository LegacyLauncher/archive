package ru.turikhay.tlauncher.ui.versions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JPopupMenu;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.ImageButton;

public class VersionRefreshButton extends ImageButton implements VersionHandlerListener, Blockable {
	private static final long serialVersionUID = -7148657244927244061L;
	
	private static final String
		PREFIX = "version.manager.refresher.",
		
		MENU = PREFIX + "menu.";
	
	final VersionHandler handler;
	
	private final JPopupMenu menu;
	private final LocalizableMenuItem local, remote;
	
	private ButtonState state;
	
	VersionRefreshButton(VersionList list) {
		this.handler = list.handler;
		
		this.menu = new JPopupMenu();
		
		this.local = new LocalizableMenuItem(MENU + "local");
		local.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handler.refresh();
			}
		});
		menu.add(local);
		
		this.remote = new LocalizableMenuItem(MENU + "remote");
		remote.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handler.asyncRefresh();
			}
		});
		menu.add(remote);
		
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onPressed();
			}
		});
		
		setState(ButtonState.REFRESH);
		handler.addListener(this);
	}
	
	void onPressed() {
		switch(state) {
		case CANCEL:
			handler.stopRefresh();
			break;
		case REFRESH:
			menu.show(this, 0, getHeight());
			break;
		}
	}
	
	private void setState(ButtonState state) {
		if(state == null)
			throw new NullPointerException();
		
		this.state = state;
		this.setImage(state.image);
	}

	@Override
	public void onVersionRefreshing(VersionManager vm) {
		setState(ButtonState.CANCEL);
	}

	@Override
	public void onVersionRefreshed(VersionManager vm) {
		setState(ButtonState.REFRESH);
	}

	@Override
	public void onVersionSelected(List<VersionSyncInfo> versions) {
	}
	

	@Override
	public void onVersionDeselected() {
	}

	@Override
	public void onVersionDownload(List<VersionSyncInfo> list) {
	}
	
	@Override
	public void block(Object reason) {
		if(!reason.equals(VersionHandler.REFRESH_BLOCK))
			this.setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		this.setEnabled(true);
	}
	
	enum ButtonState {
		REFRESH("refresh.png"), CANCEL("cancel.png");
		
		final Image image;
		
		ButtonState(String image) {
			this.image = ImageCache.getImage(image);
		}
	}

}
