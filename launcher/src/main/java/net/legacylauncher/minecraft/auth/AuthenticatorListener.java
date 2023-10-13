package net.legacylauncher.minecraft.auth;

import net.legacylauncher.user.User;

public interface AuthenticatorListener<U extends User> {
    void onAuthPassing(Authenticator<? extends U> var1);

    void onAuthPassingError(Authenticator<? extends U> var1, Throwable var2);

    void onAuthPassed(Authenticator<? extends U> var1);
}
