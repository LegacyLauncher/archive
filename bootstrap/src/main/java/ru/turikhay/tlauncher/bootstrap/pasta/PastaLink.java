package ru.turikhay.tlauncher.bootstrap.pasta;

import java.net.URL;

public class PastaLink {
    private final URL url;

    public PastaLink(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    static PastaLink parse(String href) throws PastaException {
        URL url;
        try {
            url = new URL(href);
        } catch (Exception e) {
            throw new PastaException("bad url: " + href, e);
        }
        return new PastaLink(url);
    }
}
