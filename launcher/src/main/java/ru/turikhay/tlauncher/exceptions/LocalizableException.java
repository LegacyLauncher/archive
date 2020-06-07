package ru.turikhay.tlauncher.exceptions;

public class LocalizableException extends Exception {
    private final String langPath;
    private final Object[] langVars;

    public LocalizableException(String message, Throwable cause, String langPath, Object... langVars) {
        super(message, cause);
        this.langPath = langPath;
        this.langVars = langVars;
    }
}
