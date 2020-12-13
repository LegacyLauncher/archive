package ru.turikhay.tlauncher.bootstrap.pasta;

public class PastaException extends Exception {
    public PastaException(String message) {
        super(message);
    }

    public PastaException(String message, Throwable cause) {
        super(message, cause);
    }
}
