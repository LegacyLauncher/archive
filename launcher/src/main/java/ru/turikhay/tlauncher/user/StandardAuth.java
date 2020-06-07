package ru.turikhay.tlauncher.user;

import java.io.IOException;

public interface StandardAuth<T extends User> extends Auth<T> {
    T authorize(String login, String password) throws AuthException, IOException;
}
