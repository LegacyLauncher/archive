package net.legacylauncher.connection;

import java.net.URL;

class UrlConnectionException extends Exception {
    public UrlConnectionException(URL url, Throwable cause) {
        super(url.toString(), cause, false, false);
    }
}
