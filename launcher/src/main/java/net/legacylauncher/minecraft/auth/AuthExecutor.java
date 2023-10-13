package net.legacylauncher.minecraft.auth;

import net.legacylauncher.user.AuthException;
import net.legacylauncher.user.User;

import java.io.IOException;

public interface AuthExecutor<U extends User> {
    Account<U> pass() throws AuthException, IOException;
}
