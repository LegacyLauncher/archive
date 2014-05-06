package ru.turikhay.tlauncher.ui.versions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
	
	private static final String
		ILLEGAL_SELECTION_BLOCK = "illegal-selection",
	
		PREFIX = "version.manager.delete.",

		ERROR = PREFIX + "error.",
		ERROR_TITLE = ERROR + "title",
		
		MENU = PREFIX + "menu.";
	
	private final VersionHandler handler;
	
	private final JPopupMenu menu;
	private final LocalizableMenuItem onlyJar, withLibraries;
	
	private boolean libraries;
	
	VersionRemoveButton(VersionList list) {
		super("remove.png");
		
		this.handler = list.handler;
		handler.addListener(this);
		
		this.menu = new JPopupMenu();
		
		this.onlyJar = new LocalizableMenuItem(MENU + "jar");
		onlyJar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onChosen(false);
			}
		});
		menu.add(onlyJar);
		
		this.withLibraries = new LocalizableMenuItem(MENU + "libraries");
		withLibraries.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onChosen(true);
			}
		});
		menu.add(withLibraries);
		
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onPressed();
			}
		});
	}
	
	void onPressed() {
		menu.show(this, 0, getHeight());
	}
	
	void onChosen(boolean removeLibraries) {
		this.libraries = removeLibraries;
		handler.thread.deleteThread.iterate();
	}
	
	void delete() {
		if(handler.selected != null) {
			LocalVersionList localList = handler.vm.getLocalList();
			List<Throwable> errors = new ArrayList<Throwable>();
			
			for(VersionSyncInfo version : handler.selected)
				if(version.isInstalled())
					try {
						localList.deleteVersion(version.getID(), libraries);
					} catch (Throwable e) {
						errors.add(e);
					}
			
			if(!errors.isEmpty()) {
				String title = Localizable.get(ERROR_TITLE);
				String message = Localizable.get(ERROR + (errors.size() == 1? "single" : "multiply"), errors);
				
				Alert.showError(title, message);
			}
		}
		
		handler.refresh();
	}

	@Override
	public void onVersionRefreshing(VersionManager vm) {
	}

	@Override
	public void onVersionRefreshed(VersionManager vm) {
	}

	@Override
	public void onVersionSelected(List<VersionSyncInfo> versions) {
		boolean onlyRemote = true;
		
		for(VersionSyncInfo version : versions)
			if(version.isInstalled()) {
				onlyRemote = false;
				break;
			}
		
		Blocker.setBlocked(this, ILLEGAL_SELECTION_BLOCK, onlyRemote);
	}

	@Override
	public void onVersionDeselected() {
		Blocker.block(this, ILLEGAL_SELECTION_BLOCK);
	}

	@Override
	public void onVersionDownload(List<VersionSyncInfo> list) {
	}
	
	@Override
	public void block(Object reason) {
		this.setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		this.setEnabled(true);
	}

}
