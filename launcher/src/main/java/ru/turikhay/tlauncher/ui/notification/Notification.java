package ru.turikhay.tlauncher.ui.notification;

public class Notification {
    final String image;
    final NotificationListener listener;

    public Notification(String image, NotificationListener listener) {
        this.image = image;
        this.listener = listener;
    }
}
