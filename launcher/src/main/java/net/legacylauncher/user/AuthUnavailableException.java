package net.legacylauncher.user;

import java.io.IOException;

public class AuthUnavailableException extends AuthException {
    AuthUnavailableException(String message) {
        super(message, "unavailable", message);
    }

    AuthUnavailableException(IOException ioE) {
        super(ioE, "unavailable");
    }
}
