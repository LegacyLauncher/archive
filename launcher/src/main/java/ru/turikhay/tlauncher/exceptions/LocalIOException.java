package ru.turikhay.tlauncher.exceptions;

import java.io.IOException;

public class LocalIOException extends IOException {
    public LocalIOException(IOException cause) {
        super(cause);
    }
}
