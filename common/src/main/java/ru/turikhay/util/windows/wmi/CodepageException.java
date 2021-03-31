package ru.turikhay.util.windows.wmi;

public class CodepageException extends Exception {
    public CodepageException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodepageException(String message) {
        super(message);
    }

    public CodepageException(Throwable cause) {
        super(cause);
    }
}
