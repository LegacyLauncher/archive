package ru.turikhay.util;

public class LazyInitException extends RuntimeException {
    LazyInitException(Exception cause) {
        super(null, cause, true, false);
    }
}
