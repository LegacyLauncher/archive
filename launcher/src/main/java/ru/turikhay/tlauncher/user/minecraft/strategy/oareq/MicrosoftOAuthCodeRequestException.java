package ru.turikhay.tlauncher.user.minecraft.strategy.oareq;

import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;

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
