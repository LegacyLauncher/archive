package ru.turikhay.tlauncher.user;

import ru.turikhay.util.StringUtil;

public class AuthException extends Exception {
    private final String locPath;
    private final Object[] locVars;
    protected boolean softException;

    AuthException(Throwable t, String locPath, Object... locVars) {
        super(t);
        this.locPath = StringUtil.requireNotBlank(locPath);
        this.locVars = locVars;
    }

    AuthException(String message, String locPath, Object... locVars) {
        super(message);
        this.locPath = StringUtil.requireNotBlank(locPath);
        this.locVars = locVars;
    }

    public final String getLocPath() {
        return locPath;
    }

    public final Object[] getLocVars() {
        return locVars;
    }

    public boolean isSoft() {
        return softException;
    }

    public static AuthException soft(AuthException e) {
        e.softException = true;
        return e;
    }
}
