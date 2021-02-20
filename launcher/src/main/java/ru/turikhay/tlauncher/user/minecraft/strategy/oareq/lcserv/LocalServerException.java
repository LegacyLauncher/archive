package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv;

public class LocalServerException extends Exception {
    public LocalServerException(String message) {
        super(message);
    }

    public LocalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
