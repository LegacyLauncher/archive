package ru.turikhay.util;

class SuppressedSwingException extends RuntimeException {
    public SuppressedSwingException(Throwable cause) {
        super(cause.toString(), cause);
    }
}
