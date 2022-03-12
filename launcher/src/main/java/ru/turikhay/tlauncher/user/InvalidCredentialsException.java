package ru.turikhay.tlauncher.user;

public class InvalidCredentialsException extends AuthException {
    InvalidCredentialsException(String detailed) {
        super(detailed, "invalid-credentials");
    }

    InvalidCredentialsException(String detailed, String locPath) {
        super(detailed, locPath);
    }
}
