package ru.turikhay.util;

public class JavaVersionNotDetectedException extends Exception {
    public JavaVersionNotDetectedException(String line) {
        super(line);
    }

    public JavaVersionNotDetectedException(Throwable cause) {
        super(cause);
    }

    public JavaVersionNotDetectedException() {
    }
}
