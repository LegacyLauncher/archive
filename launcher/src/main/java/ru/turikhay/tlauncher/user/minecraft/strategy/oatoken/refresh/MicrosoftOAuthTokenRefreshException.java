package ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.refresh;

import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MicrosoftOAuthTokenRefreshException extends MinecraftAuthenticationException {
    public MicrosoftOAuthTokenRefreshException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "microsoft_oauth_token_refresh";
    }
}
