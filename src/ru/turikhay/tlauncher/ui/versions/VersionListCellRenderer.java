package ru.turikhay.tlauncher.ui.versions;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.VersionCellRenderer;

public class VersionListCellRenderer extends VersionCellRenderer {
	private final VersionHandler handler;
	private final ImageIcon downloading;

	VersionListCellRenderer(VersionList list) {
		this.handler = list.handler;

		this.downloading = ImageCache.getIcon("down.png", 16, 16);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends VersionSyncInfo> list, VersionSyncInfo value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if(value == null)
			return null;

		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if(value.isInstalled() && !value.isUpToDate())
			label.setText(label.getText() + ' ' + Localizable.get("version.list.needsupdate"));

		if(handler.downloading != null) {
			for(VersionSyncInfo compare : handler.downloading) {
				ImageIcon icon = compare.equals(value)? downloading : null;

				label.setIcon(icon);
				label.setDisabledIcon(icon);

				break;
			}
		}

		return label;
	}

}
