package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class SideNotifier extends ExtendedPanel {
    public static final int GAP = SwingUtil.magnify(10);

    private final int buttonSize = SwingUtil.magnify(48);
    private final int iconSize = SwingUtil.magnify(32);
    public final int height = GAP * 2 + buttonSize;

    private final ArrayList<NotificationIcon> notifications = new ArrayList<>();

    public SideNotifier() {
        setLayout(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
    }

    public void addNotification(String id, Notification notification) {
        removeNotification(id);

        ExtendedButton button = new ExtendedButton();
        button.addActionListener(e -> notification.listener.onClicked());
        button.setPreferredSize(new Dimension(buttonSize, buttonSize));

        NotificationIcon icon = new NotificationIcon(id, notification, iconSize, button);
        notifications.add(icon);
        ImageIcon.setup(button, icon);

        add(button);
        revalidate();
    }

    public void removeNotification(String id) {
        Iterator<NotificationIcon> i = notifications.iterator();
        while(i.hasNext()) {
            NotificationIcon n = i.next();
            if(n.id.equals(id)) {
                remove(n.parent);
                i.remove();
            }
        }
        revalidate();
    }

    public interface NotificationListener {
        void onClicked();
    }

    public static class Notification {
        final String image;
        final NotificationListener listener;

        public Notification(String image, NotificationListener listener) {
            this.image = image;
            this.listener = listener;
        }
    }

    private static class NotificationIcon extends ImageIcon {
        final ExtendedButton parent;
        final String id;

        NotificationIcon(String id, Notification notification, int iconSize, ExtendedButton parent) {
            super(Images.loadIcon(notification.image, iconSize), iconSize);
            this.id = id;
            this.parent = parent;
        }
    }
}
