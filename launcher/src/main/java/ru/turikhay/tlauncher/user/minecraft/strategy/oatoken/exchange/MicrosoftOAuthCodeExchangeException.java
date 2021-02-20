package ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.exchange;

import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MicrosoftOAuthCodeExchangeException extends MinecraftAuthenticationException {

    public MicrosoftOAuthCodeExchangeException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "microsoft_oauth_code_exchange";
    }
}
