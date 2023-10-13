package net.legacylauncher.user.minecraft.strategy.oareq;

import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MicrosoftOAuthCodeRequestException extends MinecraftAuthenticationException {
    public MicrosoftOAuthCodeRequestException(String message) {
        super(message);
    }

    public MicrosoftOAuthCodeRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getShortKey() {
        return "microsoft_oauth_code_request";
    }
}
