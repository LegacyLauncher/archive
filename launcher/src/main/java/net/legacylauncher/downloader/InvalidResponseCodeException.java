package net.legacylauncher.downloader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Locale;

public class InvalidResponseCodeException extends IOException {
    private final int statusCode;

    public InvalidResponseCodeException(String url, int statusCode, @Nullable Object expectedCode) {
        super(String.format(Locale.ROOT, "[%d%s] %s", statusCode, expectedCode == null ? "" : " != " + expectedCode, url));
        this.statusCode = statusCode;
    }

    public InvalidResponseCodeException(String url, int statusCode) {
        this(url, statusCode, null);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode <= 499;
    }
}
