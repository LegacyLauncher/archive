package net.legacylauncher.common.exceptions;

import java.io.IOException;

public class LocalIOException extends IOException {
    public LocalIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalIOException(Throwable cause) {
        super(cause);
    }

    public LocalIOException(String message) {
        super(message);
    }
}
