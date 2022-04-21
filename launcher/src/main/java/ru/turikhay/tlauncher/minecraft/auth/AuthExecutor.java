package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.user.AuthException;
import ru.turikhay.tlauncher.user.User;

import java.io.IOException;

public interface AuthExecutor<U extends User> {
    Account<U> pass() throws AuthException, IOException;
}
