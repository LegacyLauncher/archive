package ru.turikhay.tlauncher.user;

public class AuthUnavailableException extends AuthException {
    AuthUnavailableException(String message) {
        super(message, "unavailable", message);
    }
}
