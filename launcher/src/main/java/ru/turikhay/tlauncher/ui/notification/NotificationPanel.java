package ru.turikhay.tlauncher.ui.notification;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class NotificationPanel extends ExtendedPanel implements LocalizableComponent {
    public static final int GAP = SwingUtil.magnify(10);

    private final int buttonSize = SwingUtil.magnify(48);
    private final int iconSize = SwingUtil.magnify(32);
    public final int height = GAP * 2 + buttonSize;

    private final ArrayList<NotificationIcon> notifications = new ArrayList<>();

    public NotificationPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        updateLocale();
    }

    public void addNotification(String id, Notification notification) {
        removeNotification(id);

        ExtendedButton button = new ExtendedButton();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    removeNotification(id);
                }
            }
        });
        button.addActionListener(e -> notification.listener.onClicked());
        button.setPreferredSize(new Dimension(buttonSize, buttonSize));

        NotificationIcon icon = new NotificationIcon(id, notification, iconSize, button);
        notifications.add(icon);
        ImageIcon.setup(button, icon);

        add(button);
        deferUpdate();
    }

    public void removeNotification(String id) {
        Iterator<NotificationIcon> i = notifications.iterator();
        boolean removed = false;
        while (i.hasNext()) {
            NotificationIcon n = i.next();
            if (n.id.equals(id)) {
                remove(n.parent);
                i.remove();
                removed = true;
            }
        }
        if (removed) {
            deferUpdate();
        }
    }

    private void deferUpdate() {
        SwingUtil.later(() -> {
            revalidate();
            repaint();
        });
    }

    private String localeNotificationId;

    @Override
    public void updateLocale() {
        if (localeNotificationId != null) {
            removeNotification(localeNotificationId);
            localeNotificationId = null;
        }
        UrlNotificationObject notificationObject = TLauncher.getInstance().getBootConfig()
                .getNotifications().get(Localizable.get().getLocale().toString());
        if (notificationObject != null) {
            addNotification(notificationObject.getId(), new Notification(
                    notificationObject.getImage(),
                    new NotificationListener.UrlOpen(notificationObject.getUrl())
                            .then(new NotificationListener.Remove(this, notificationObject.getId()))
            ));
            localeNotificationId = notificationObject.getId();
        }
    }
}
