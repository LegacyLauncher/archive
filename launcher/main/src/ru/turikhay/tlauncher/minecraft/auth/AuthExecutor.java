package ru.turikhay.tlauncher.minecraft.auth;

import ru.turikhay.tlauncher.user.AuthException;

import java.io.IOException;

public interface AuthExecutor {
    Account pass() throws AuthException, IOException;
}
