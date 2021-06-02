package ru.turikhay.tlauncher.pasta;

import java.io.IOException;

public class PastaException extends IOException {
    public PastaException() {
    }

    public PastaException(String message) {
        super(message);
    }

    public PastaException(String message, Throwable cause) {
        super(message, cause);
    }
}
