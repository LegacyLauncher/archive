package ru.turikhay.tlauncher.user.minecraft.strategy.oareq;

import java.net.MalformedURLException;
import java.net.URL;

public class RedirectUrl {
    private final URL url;

    public RedirectUrl(URL url) {
        this.url = url;
    }

    public RedirectUrl(String url) throws MalformedURLException {
        this(new URL(url));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RedirectUrl that = (RedirectUrl) o;

        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "RedirectUrl{" +
                "uri=" + url +
                '}';
    }

    public static RedirectUrl of(String url) {
        try {
            return new RedirectUrl(url);
        } catch (MalformedURLException e) {
            throw new Error("invalid url: " + url, e);
        }
    }
}
