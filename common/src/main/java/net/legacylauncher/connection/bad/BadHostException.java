package net.legacylauncher.connection.bad;

import java.io.IOException;
import java.net.URL;

public class BadHostException extends IOException {
    public BadHostException(URL url) {
        super(url.getHost());
    }
}
