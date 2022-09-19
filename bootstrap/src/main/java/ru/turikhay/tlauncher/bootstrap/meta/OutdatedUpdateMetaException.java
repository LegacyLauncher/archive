package ru.turikhay.tlauncher.bootstrap.meta;

import java.io.IOException;
import java.util.Locale;

class OutdatedUpdateMetaException extends IOException {
    public OutdatedUpdateMetaException(String url, String currentTime, String expiryTime) {
        super(String.format(Locale.ROOT, "url: %s, current time: %s, expires at: %s",
                url, currentTime, expiryTime), null);
        setStackTrace(new StackTraceElement[0]);
    }
}
