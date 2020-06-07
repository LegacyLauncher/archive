package ru.turikhay.util.async;

class AsyncObjectException extends Exception {
    private static final long serialVersionUID = 1L;

    AsyncObjectException() {
    }

    AsyncObjectException(Throwable cause) {
        super(cause);
    }

}
