package ru.turikhay.tlauncher.ui.notification;

import ru.turikhay.util.OS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface NotificationListener {
    void onClicked();

    default NotificationListener then(NotificationListener... listeners) {
        ArrayList<NotificationListener> l = new ArrayList<>(listeners.length + 1);
        l.add(this);
        l.addAll(Arrays.asList(listeners));
        return new NotificationListenerChain(l);
    }

    class NotificationListenerChain implements NotificationListener {
        private final List<NotificationListener> listeners;

        public NotificationListenerChain(List<NotificationListener> listeners) {
            this.listeners = listeners;
        }

        @Override
        public void onClicked() {
            for (NotificationListener listener : listeners) {
                listener.onClicked();
            }
        }
    }

    class UrlOpen implements NotificationListener {
        private final String url;

        public UrlOpen(String url) {
            this.url = url;
        }

        @Override
        public void onClicked() {
            OS.openLink(url);
        }
    }

    class Remove implements NotificationListener {
        private final NotificationPanel panel;
        private final String id;

        public Remove(NotificationPanel panel, String id) {
            this.panel = panel;
            this.id = id;
        }

        @Override
        public void onClicked() {
            panel.removeNotification(id);
        }
    }
}
