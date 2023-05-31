package ru.turikhay.util;

public class LazyInitException extends RuntimeException {
    LazyInitException(Throwable cause) {
        super(null, cause, true, false);
    }
}
