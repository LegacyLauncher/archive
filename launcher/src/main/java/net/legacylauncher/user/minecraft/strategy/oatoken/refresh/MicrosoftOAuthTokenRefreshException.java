package net.legacylauncher.user.minecraft.strategy.oatoken.refresh;

import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MicrosoftOAuthTokenRefreshException extends MinecraftAuthenticationException {
    public MicrosoftOAuthTokenRefreshException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "microsoft_oauth_token_refresh";
    }
}
