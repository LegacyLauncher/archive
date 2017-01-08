package ru.turikhay.tlauncher.minecraft.auth;

public class KnownAuthenticatorException extends AuthenticatorException {
    KnownAuthenticatorException(String message, String langpath) {
        super(message, langpath);
    }

    KnownAuthenticatorException(StandardAuthenticator.Response response, String langpath) {
        super(response, langpath);
    }

    KnownAuthenticatorException(String message, String langpath, Throwable cause) {
        super(message, langpath, cause);
    }
}
