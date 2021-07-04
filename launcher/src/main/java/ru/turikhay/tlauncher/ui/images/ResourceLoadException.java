package ru.turikhay.tlauncher.ui.images;

import java.net.URL;

class ResourceLoadException extends Exception {
    private final URL url;

    public ResourceLoadException(URL url, Throwable cause) {
        super(String.valueOf(url), cause);
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }
}
