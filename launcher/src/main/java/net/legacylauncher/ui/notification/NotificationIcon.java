package net.legacylauncher.ui.notification;

import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.swing.extended.ExtendedButton;

class NotificationIcon extends ImageIcon {
    final ExtendedButton parent;
    final String id;

    NotificationIcon(String id, Notification notification, int iconSize, ExtendedButton parent) {
        super(notification.image, iconSize);
        this.id = id;
        this.parent = parent;
    }
}
