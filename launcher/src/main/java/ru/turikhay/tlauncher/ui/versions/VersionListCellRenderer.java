package ru.turikhay.tlauncher.ui.versions;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.VersionCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

public class VersionListCellRenderer extends VersionCellRenderer {
    private final VersionHandler handler;
    private final ImageIcon downloading;

    VersionListCellRenderer(VersionList list) {
        handler = list.handler;
        downloading = Images.getIcon("download.png", 16, 16);
    }

    public Component getListCellRendererComponent(JList<? extends VersionSyncInfo> list, VersionSyncInfo value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return null;
        } else {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value.isInstalled() && !value.isUpToDate()) {
                label.setText(label.getText() + ' ' + Localizable.get("version.list.needsupdate"));
            }

            if (handler.downloading != null) {
                Iterator var8 = handler.downloading.iterator();
                if (var8.hasNext()) {
                    VersionSyncInfo compare = (VersionSyncInfo) var8.next();
                    ImageIcon icon = compare.equals(value) ? downloading : null;
                    label.setIcon(icon);
                    label.setDisabledIcon(icon);
                }
            }

            return label;
        }
    }
}
