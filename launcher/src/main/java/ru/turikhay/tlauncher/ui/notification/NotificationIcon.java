package ru.turikhay.tlauncher.ui.notification;

import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;

class NotificationIcon extends ImageIcon {
    final ExtendedButton parent;
    final String id;

    NotificationIcon(String id, Notification notification, int iconSize, ExtendedButton parent) {
        super(Images.loadIcon(notification.image, iconSize), iconSize);
        this.id = id;
        this.parent = parent;
    }
}
