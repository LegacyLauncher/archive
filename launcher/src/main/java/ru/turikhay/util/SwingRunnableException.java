package ru.turikhay.util;

class SwingRunnableException extends RuntimeException {
    public SwingRunnableException(Throwable cause) {
        super(cause.toString(), cause, false, false);
    }
}
