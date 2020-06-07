package ru.turikhay.tlauncher.user;

import ru.turikhay.tlauncher.exceptions.LocalizableException;

public class ElyAuthStrategyException extends LocalizableException {
    public ElyAuthStrategyException(String message, Throwable cause, String langPath, Object... langVars) {
        super(message, cause, langPath, langVars);
    }
}
