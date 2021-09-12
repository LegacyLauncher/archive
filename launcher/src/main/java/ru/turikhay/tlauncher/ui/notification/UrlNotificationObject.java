package ru.turikhay.tlauncher.ui.notification;

import java.util.Objects;

public class UrlNotificationObject {
    String id, image, url;

    public String getId() {
        return Objects.requireNonNull(id);
    }

    public String getImage() {
        return Objects.requireNonNull(image);
    }

    public String getUrl() {
        return Objects.requireNonNull(url);
    }
}
